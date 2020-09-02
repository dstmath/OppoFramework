package android.net.wifi;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.ParceledListSlice;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.wifi.IDppCallback;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.IOnWifiUsabilityStatsListener;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.IStaStateCallback;
import android.net.wifi.ITrafficStateCallback;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.WorkSource;
import java.util.List;
import java.util.Map;
import oppo.net.wifi.HotspotClient;

public interface IWifiManager extends IInterface {
    void acquireMulticastLock(IBinder iBinder, String str) throws RemoteException;

    boolean acquireWifiLock(IBinder iBinder, int i, String str, WorkSource workSource) throws RemoteException;

    int addNetworkSuggestions(List<WifiNetworkSuggestion> list, String str) throws RemoteException;

    void addOnWifiUsabilityStatsListener(IBinder iBinder, IOnWifiUsabilityStatsListener iOnWifiUsabilityStatsListener, int i) throws RemoteException;

    int addOrUpdateNetwork(WifiConfiguration wifiConfiguration, String str) throws RemoteException;

    boolean addOrUpdatePasspointConfiguration(PasspointConfiguration passpointConfiguration, String str) throws RemoteException;

    boolean allowDevice(String str, String str2) throws RemoteException;

    boolean blockClient(HotspotClient hotspotClient) throws RemoteException;

    void clearWifiOCloudData(boolean z) throws RemoteException;

    void deauthenticateNetwork(long j, boolean z) throws RemoteException;

    void disableDualSta() throws RemoteException;

    void disableEphemeralNetwork(String str, String str2) throws RemoteException;

    boolean disableNetwork(int i, String str) throws RemoteException;

    boolean disallowDevice(String str) throws RemoteException;

    boolean disconnect(String str) throws RemoteException;

    int enableDualSta() throws RemoteException;

    int enableDualStaByForce(boolean z) throws RemoteException;

    boolean enableNetwork(int i, boolean z, String str) throws RemoteException;

    void enableTdls(String str, boolean z) throws RemoteException;

    void enableTdlsWithMacAddress(String str, boolean z) throws RemoteException;

    void enableVerboseLogging(int i) throws RemoteException;

    void enableWifiConnectivityManager(boolean z) throws RemoteException;

    void factoryReset(String str) throws RemoteException;

    String[] getAllDualStaApps() throws RemoteException;

    Map getAllMatchingFqdnsForScanResults(List<ScanResult> list) throws RemoteException;

    String[] getAllSlaAcceleratedApps() throws RemoteException;

    String[] getAllSlaAppsAndStates() throws RemoteException;

    List<HotspotClient> getAllowedDevices() throws RemoteException;

    List<HotspotClient> getBlockedHotspotClients() throws RemoteException;

    String getClientDeviceName(String str) throws RemoteException;

    String getClientIp(String str) throws RemoteException;

    ParceledListSlice getConfiguredNetworks(String str) throws RemoteException;

    WifiInfo getConnectionInfo(String str) throws RemoteException;

    String getCountryCode() throws RemoteException;

    @UnsupportedAppUsage
    Network getCurrentNetwork() throws RemoteException;

    String getCurrentNetworkWpsNfcConfigurationToken() throws RemoteException;

    DhcpInfo getDhcpInfo() throws RemoteException;

    String[] getFactoryMacAddresses() throws RemoteException;

    List<HotspotClient> getHotspotClients() throws RemoteException;

    Map getMatchingOsuProviders(List<ScanResult> list) throws RemoteException;

    Map getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> list) throws RemoteException;

    boolean getNetworkEverCaptiveState() throws RemoteException;

    WifiInfo getOppoSta2ConnectionInfo(String str) throws RemoteException;

    String getOppoSta2CurConfigKey() throws RemoteException;

    List<PasspointConfiguration> getPasspointConfigurations(String str) throws RemoteException;

    ParceledListSlice getPrivilegedConfiguredNetworks(String str) throws RemoteException;

    List<ScanResult> getScanResults(String str) throws RemoteException;

    boolean getSlaAppState(String str) throws RemoteException;

    long getSupportedFeatures() throws RemoteException;

    int getVerboseLoggingLevel() throws RemoteException;

    @UnsupportedAppUsage
    WifiConfiguration getWifiApConfiguration() throws RemoteException;

    @UnsupportedAppUsage
    int getWifiApEnabledState() throws RemoteException;

    List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> list) throws RemoteException;

    int getWifiEnabledState() throws RemoteException;

    List<String> getWifiOCloudData(boolean z) throws RemoteException;

    String getWifiPowerEventCode() throws RemoteException;

    Messenger getWifiServiceMessenger(String str) throws RemoteException;

    WifiConfiguration getWifiSharingConfiguration() throws RemoteException;

    boolean hasOCloudDirtyData() throws RemoteException;

    void initializeMulticastFiltering() throws RemoteException;

    boolean isAllDevicesAllowed() throws RemoteException;

    boolean isDualBandSupported() throws RemoteException;

    boolean isDualStaSupported() throws RemoteException;

    boolean isMulticastEnabled() throws RemoteException;

    boolean isP2p5GSupported() throws RemoteException;

    boolean isScanAlwaysAvailable() throws RemoteException;

    boolean isSlaSupported() throws RemoteException;

    boolean isSoftap5GSupported() throws RemoteException;

    int matchProviderWithCurrentNetwork(String str) throws RemoteException;

    boolean needs5GHzToAnyApBandConversion() throws RemoteException;

    void notifyUserOfApBandConversion(String str) throws RemoteException;

    void queryPasspointIcon(long j, String str) throws RemoteException;

    boolean reassociate(String str) throws RemoteException;

    boolean reconnect(String str) throws RemoteException;

    void registerNetworkRequestMatchCallback(IBinder iBinder, INetworkRequestMatchCallback iNetworkRequestMatchCallback, int i) throws RemoteException;

    void registerSoftApCallback(IBinder iBinder, ISoftApCallback iSoftApCallback, int i) throws RemoteException;

    void registerStaStateCallback(IBinder iBinder, IStaStateCallback iStaStateCallback, int i) throws RemoteException;

    void registerTrafficStateCallback(IBinder iBinder, ITrafficStateCallback iTrafficStateCallback, int i) throws RemoteException;

    void releaseMulticastLock(String str) throws RemoteException;

    boolean releaseWifiLock(IBinder iBinder) throws RemoteException;

    boolean removeNetwork(int i, String str) throws RemoteException;

    void removeNetworkByGlobalId(String str, String str2, boolean z) throws RemoteException;

    int removeNetworkSuggestions(List<WifiNetworkSuggestion> list, String str) throws RemoteException;

    void removeOnWifiUsabilityStatsListener(int i) throws RemoteException;

    boolean removePasspointConfiguration(String str, String str2) throws RemoteException;

    WifiActivityEnergyInfo reportActivityInfo() throws RemoteException;

    void requestActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    void restoreBackupData(byte[] bArr) throws RemoteException;

    void restoreSupplicantBackupData(byte[] bArr, byte[] bArr2) throws RemoteException;

    byte[] retrieveBackupData() throws RemoteException;

    boolean setAllDevicesAllowed(boolean z, boolean z2) throws RemoteException;

    void setCountryCode(String str) throws RemoteException;

    void setDeviceMobilityState(int i) throws RemoteException;

    void setDirtyFlag(String str, boolean z) throws RemoteException;

    void setNetworkCaptiveState(boolean z) throws RemoteException;

    void setPowerSavingMode(boolean z) throws RemoteException;

    boolean setSlaAppState(String str, boolean z) throws RemoteException;

    boolean setWifiApConfiguration(WifiConfiguration wifiConfiguration, String str) throws RemoteException;

    boolean setWifiEnabled(String str, boolean z) throws RemoteException;

    boolean setWifiSharingConfiguration(WifiConfiguration wifiConfiguration) throws RemoteException;

    void startApWps(WpsInfo wpsInfo) throws RemoteException;

    void startDppAsConfiguratorInitiator(IBinder iBinder, String str, int i, int i2, IDppCallback iDppCallback) throws RemoteException;

    void startDppAsEnrolleeInitiator(IBinder iBinder, String str, IDppCallback iDppCallback) throws RemoteException;

    int startLocalOnlyHotspot(Messenger messenger, IBinder iBinder, String str) throws RemoteException;

    boolean startScan(String str) throws RemoteException;

    boolean startSoftAp(WifiConfiguration wifiConfiguration) throws RemoteException;

    void startSubscriptionProvisioning(OsuProvider osuProvider, IProvisioningCallback iProvisioningCallback) throws RemoteException;

    void startWatchLocalOnlyHotspot(Messenger messenger, IBinder iBinder) throws RemoteException;

    boolean startWifiSharing(WifiConfiguration wifiConfiguration) throws RemoteException;

    void stopDppSession() throws RemoteException;

    void stopLocalOnlyHotspot() throws RemoteException;

    boolean stopSoftAp() throws RemoteException;

    void stopWatchLocalOnlyHotspot() throws RemoteException;

    boolean stopWifiSharing() throws RemoteException;

    boolean unblockClient(HotspotClient hotspotClient) throws RemoteException;

    void unregisterNetworkRequestMatchCallback(int i) throws RemoteException;

    void unregisterSoftApCallback(int i) throws RemoteException;

    void unregisterStaStateCallback(int i) throws RemoteException;

    void unregisterTrafficStateCallback(int i) throws RemoteException;

    void updateGlobalId(int i, String str) throws RemoteException;

    void updateInterfaceIpState(String str, int i) throws RemoteException;

    void updateWifiLockWorkSource(IBinder iBinder, WorkSource workSource) throws RemoteException;

    void updateWifiUsabilityScore(int i, int i2, int i3) throws RemoteException;

    public static class Default implements IWifiManager {
        @Override // android.net.wifi.IWifiManager
        public long getSupportedFeatures() throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public WifiActivityEnergyInfo reportActivityInfo() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void requestActivityInfo(ResultReceiver result) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public ParceledListSlice getConfiguredNetworks(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public ParceledListSlice getPrivilegedConfiguredNetworks(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public Map getAllMatchingFqdnsForScanResults(List<ScanResult> list) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public Map getMatchingOsuProviders(List<ScanResult> list) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public Map getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> list) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public int addOrUpdateNetwork(WifiConfiguration config, String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean removePasspointConfiguration(String fqdn, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public List<PasspointConfiguration> getPasspointConfigurations(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> list) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void queryPasspointIcon(long bssid, String fileName) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public int matchProviderWithCurrentNetwork(String fqdn) throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public void deauthenticateNetwork(long holdoff, boolean ess) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean removeNetwork(int netId, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean enableNetwork(int netId, boolean disableOthers, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean disableNetwork(int netId, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean startScan(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public List<ScanResult> getScanResults(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean disconnect(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean reconnect(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean reassociate(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public WifiInfo getConnectionInfo(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean setWifiEnabled(String packageName, boolean enable) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public int getWifiEnabledState() throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public void setCountryCode(String country) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public String getCountryCode() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isDualBandSupported() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean needs5GHzToAnyApBandConversion() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public DhcpInfo getDhcpInfo() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isScanAlwaysAvailable() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean acquireWifiLock(IBinder lock, int lockType, String tag, WorkSource ws) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void updateWifiLockWorkSource(IBinder lock, WorkSource ws) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean releaseWifiLock(IBinder lock) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void initializeMulticastFiltering() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isMulticastEnabled() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void acquireMulticastLock(IBinder binder, String tag) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void releaseMulticastLock(String tag) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void updateInterfaceIpState(String ifaceName, int mode) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean startSoftAp(WifiConfiguration wifiConfig) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean stopSoftAp() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public void stopLocalOnlyHotspot() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void stopWatchLocalOnlyHotspot() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public int getWifiApEnabledState() throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public WifiConfiguration getWifiApConfiguration() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean setWifiApConfiguration(WifiConfiguration wifiConfig, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void notifyUserOfApBandConversion(String packageName) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public Messenger getWifiServiceMessenger(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void enableTdls(String remoteIPAddress, boolean enable) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public String getCurrentNetworkWpsNfcConfigurationToken() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void enableVerboseLogging(int verbose) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public int getVerboseLoggingLevel() throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public void enableWifiConnectivityManager(boolean enabled) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void disableEphemeralNetwork(String SSID, String packageName) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void factoryReset(String packageName) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public Network getCurrentNetwork() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public byte[] retrieveBackupData() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void restoreBackupData(byte[] data) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void registerSoftApCallback(IBinder binder, ISoftApCallback callback, int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void unregisterSoftApCallback(int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void addOnWifiUsabilityStatsListener(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void removeOnWifiUsabilityStatsListener(int listenerIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void registerTrafficStateCallback(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void unregisterTrafficStateCallback(int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean setSlaAppState(String pkgName, boolean enabled) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean getSlaAppState(String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public String[] getAllSlaAppsAndStates() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public String[] getAllSlaAcceleratedApps() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isSlaSupported() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public String[] getAllDualStaApps() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isDualStaSupported() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public WifiInfo getOppoSta2ConnectionInfo(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public String getOppoSta2CurConfigKey() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public int enableDualSta() throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public int enableDualStaByForce(boolean force) throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public void disableDualSta() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void registerNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void unregisterNetworkRequestMatchCallback(int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public int addNetworkSuggestions(List<WifiNetworkSuggestion> list, String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public int removeNetworkSuggestions(List<WifiNetworkSuggestion> list, String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IWifiManager
        public String[] getFactoryMacAddresses() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void setDeviceMobilityState(int state) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void startDppAsConfiguratorInitiator(IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void startDppAsEnrolleeInitiator(IBinder binder, String configuratorUri, IDppCallback callback) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void stopDppSession() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public String getWifiPowerEventCode() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void setNetworkCaptiveState(boolean captiveState) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean getNetworkEverCaptiveState() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void startApWps(WpsInfo config) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public List<HotspotClient> getHotspotClients() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public List<HotspotClient> getBlockedHotspotClients() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public String getClientIp(String deviceAddress) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public String getClientDeviceName(String deviceAddress) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean blockClient(HotspotClient client) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean unblockClient(HotspotClient client) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isAllDevicesAllowed() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean setAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean allowDevice(String deviceAddress, String name) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean disallowDevice(String deviceAddress) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public List<HotspotClient> getAllowedDevices() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean startWifiSharing(WifiConfiguration wifiConfig) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean stopWifiSharing() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public WifiConfiguration getWifiSharingConfiguration() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean setWifiSharingConfiguration(WifiConfiguration wifiConfig) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void clearWifiOCloudData(boolean hardDelete) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public List<String> getWifiOCloudData(boolean isDirtyOnly) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWifiManager
        public void updateGlobalId(int itemId, String globalId) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void removeNetworkByGlobalId(String configKey, String globalId, boolean hardDelete) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void setDirtyFlag(String globalId, boolean value) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public boolean hasOCloudDirtyData() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isP2p5GSupported() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public boolean isSoftap5GSupported() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWifiManager
        public void registerStaStateCallback(IBinder binder, IStaStateCallback callback, int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void unregisterStaStateCallback(int callbackIdentifier) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiManager
        public void setPowerSavingMode(boolean mode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiManager {
        private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
        static final int TRANSACTION_acquireMulticastLock = 39;
        static final int TRANSACTION_acquireWifiLock = 34;
        static final int TRANSACTION_addNetworkSuggestions = 86;
        static final int TRANSACTION_addOnWifiUsabilityStatsListener = 68;
        static final int TRANSACTION_addOrUpdateNetwork = 9;
        static final int TRANSACTION_addOrUpdatePasspointConfiguration = 10;
        static final int TRANSACTION_allowDevice = 106;
        static final int TRANSACTION_blockClient = 102;
        static final int TRANSACTION_clearWifiOCloudData = 113;
        static final int TRANSACTION_deauthenticateNetwork = 16;
        static final int TRANSACTION_disableDualSta = 83;
        static final int TRANSACTION_disableEphemeralNetwork = 59;
        static final int TRANSACTION_disableNetwork = 19;
        static final int TRANSACTION_disallowDevice = 107;
        static final int TRANSACTION_disconnect = 22;
        static final int TRANSACTION_enableDualSta = 81;
        static final int TRANSACTION_enableDualStaByForce = 82;
        static final int TRANSACTION_enableNetwork = 18;
        static final int TRANSACTION_enableTdls = 53;
        static final int TRANSACTION_enableTdlsWithMacAddress = 54;
        static final int TRANSACTION_enableVerboseLogging = 56;
        static final int TRANSACTION_enableWifiConnectivityManager = 58;
        static final int TRANSACTION_factoryReset = 60;
        static final int TRANSACTION_getAllDualStaApps = 77;
        static final int TRANSACTION_getAllMatchingFqdnsForScanResults = 6;
        static final int TRANSACTION_getAllSlaAcceleratedApps = 75;
        static final int TRANSACTION_getAllSlaAppsAndStates = 74;
        static final int TRANSACTION_getAllowedDevices = 108;
        static final int TRANSACTION_getBlockedHotspotClients = 99;
        static final int TRANSACTION_getClientDeviceName = 101;
        static final int TRANSACTION_getClientIp = 100;
        static final int TRANSACTION_getConfiguredNetworks = 4;
        static final int TRANSACTION_getConnectionInfo = 25;
        static final int TRANSACTION_getCountryCode = 29;
        static final int TRANSACTION_getCurrentNetwork = 61;
        static final int TRANSACTION_getCurrentNetworkWpsNfcConfigurationToken = 55;
        static final int TRANSACTION_getDhcpInfo = 32;
        static final int TRANSACTION_getFactoryMacAddresses = 88;
        static final int TRANSACTION_getHotspotClients = 98;
        static final int TRANSACTION_getMatchingOsuProviders = 7;
        static final int TRANSACTION_getMatchingPasspointConfigsForOsuProviders = 8;
        static final int TRANSACTION_getNetworkEverCaptiveState = 96;
        static final int TRANSACTION_getOppoSta2ConnectionInfo = 79;
        static final int TRANSACTION_getOppoSta2CurConfigKey = 80;
        static final int TRANSACTION_getPasspointConfigurations = 12;
        static final int TRANSACTION_getPrivilegedConfiguredNetworks = 5;
        static final int TRANSACTION_getScanResults = 21;
        static final int TRANSACTION_getSlaAppState = 73;
        static final int TRANSACTION_getSupportedFeatures = 1;
        static final int TRANSACTION_getVerboseLoggingLevel = 57;
        static final int TRANSACTION_getWifiApConfiguration = 49;
        static final int TRANSACTION_getWifiApEnabledState = 48;
        static final int TRANSACTION_getWifiConfigsForPasspointProfiles = 13;
        static final int TRANSACTION_getWifiEnabledState = 27;
        static final int TRANSACTION_getWifiOCloudData = 114;
        static final int TRANSACTION_getWifiPowerEventCode = 94;
        static final int TRANSACTION_getWifiServiceMessenger = 52;
        static final int TRANSACTION_getWifiSharingConfiguration = 111;
        static final int TRANSACTION_hasOCloudDirtyData = 118;
        static final int TRANSACTION_initializeMulticastFiltering = 37;
        static final int TRANSACTION_isAllDevicesAllowed = 104;
        static final int TRANSACTION_isDualBandSupported = 30;
        static final int TRANSACTION_isDualStaSupported = 78;
        static final int TRANSACTION_isMulticastEnabled = 38;
        static final int TRANSACTION_isP2p5GSupported = 119;
        static final int TRANSACTION_isScanAlwaysAvailable = 33;
        static final int TRANSACTION_isSlaSupported = 76;
        static final int TRANSACTION_isSoftap5GSupported = 120;
        static final int TRANSACTION_matchProviderWithCurrentNetwork = 15;
        static final int TRANSACTION_needs5GHzToAnyApBandConversion = 31;
        static final int TRANSACTION_notifyUserOfApBandConversion = 51;
        static final int TRANSACTION_queryPasspointIcon = 14;
        static final int TRANSACTION_reassociate = 24;
        static final int TRANSACTION_reconnect = 23;
        static final int TRANSACTION_registerNetworkRequestMatchCallback = 84;
        static final int TRANSACTION_registerSoftApCallback = 66;
        static final int TRANSACTION_registerStaStateCallback = 121;
        static final int TRANSACTION_registerTrafficStateCallback = 70;
        static final int TRANSACTION_releaseMulticastLock = 40;
        static final int TRANSACTION_releaseWifiLock = 36;
        static final int TRANSACTION_removeNetwork = 17;
        static final int TRANSACTION_removeNetworkByGlobalId = 116;
        static final int TRANSACTION_removeNetworkSuggestions = 87;
        static final int TRANSACTION_removeOnWifiUsabilityStatsListener = 69;
        static final int TRANSACTION_removePasspointConfiguration = 11;
        static final int TRANSACTION_reportActivityInfo = 2;
        static final int TRANSACTION_requestActivityInfo = 3;
        static final int TRANSACTION_restoreBackupData = 63;
        static final int TRANSACTION_restoreSupplicantBackupData = 64;
        static final int TRANSACTION_retrieveBackupData = 62;
        static final int TRANSACTION_setAllDevicesAllowed = 105;
        static final int TRANSACTION_setCountryCode = 28;
        static final int TRANSACTION_setDeviceMobilityState = 89;
        static final int TRANSACTION_setDirtyFlag = 117;
        static final int TRANSACTION_setNetworkCaptiveState = 95;
        static final int TRANSACTION_setPowerSavingMode = 123;
        static final int TRANSACTION_setSlaAppState = 72;
        static final int TRANSACTION_setWifiApConfiguration = 50;
        static final int TRANSACTION_setWifiEnabled = 26;
        static final int TRANSACTION_setWifiSharingConfiguration = 112;
        static final int TRANSACTION_startApWps = 97;
        static final int TRANSACTION_startDppAsConfiguratorInitiator = 90;
        static final int TRANSACTION_startDppAsEnrolleeInitiator = 91;
        static final int TRANSACTION_startLocalOnlyHotspot = 44;
        static final int TRANSACTION_startScan = 20;
        static final int TRANSACTION_startSoftAp = 42;
        static final int TRANSACTION_startSubscriptionProvisioning = 65;
        static final int TRANSACTION_startWatchLocalOnlyHotspot = 46;
        static final int TRANSACTION_startWifiSharing = 109;
        static final int TRANSACTION_stopDppSession = 92;
        static final int TRANSACTION_stopLocalOnlyHotspot = 45;
        static final int TRANSACTION_stopSoftAp = 43;
        static final int TRANSACTION_stopWatchLocalOnlyHotspot = 47;
        static final int TRANSACTION_stopWifiSharing = 110;
        static final int TRANSACTION_unblockClient = 103;
        static final int TRANSACTION_unregisterNetworkRequestMatchCallback = 85;
        static final int TRANSACTION_unregisterSoftApCallback = 67;
        static final int TRANSACTION_unregisterStaStateCallback = 122;
        static final int TRANSACTION_unregisterTrafficStateCallback = 71;
        static final int TRANSACTION_updateGlobalId = 115;
        static final int TRANSACTION_updateInterfaceIpState = 41;
        static final int TRANSACTION_updateWifiLockWorkSource = 35;
        static final int TRANSACTION_updateWifiUsabilityScore = 93;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiManager)) {
                return new Proxy(obj);
            }
            return (IWifiManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getSupportedFeatures";
                case 2:
                    return "reportActivityInfo";
                case 3:
                    return "requestActivityInfo";
                case 4:
                    return "getConfiguredNetworks";
                case 5:
                    return "getPrivilegedConfiguredNetworks";
                case 6:
                    return "getAllMatchingFqdnsForScanResults";
                case 7:
                    return "getMatchingOsuProviders";
                case 8:
                    return "getMatchingPasspointConfigsForOsuProviders";
                case 9:
                    return "addOrUpdateNetwork";
                case 10:
                    return "addOrUpdatePasspointConfiguration";
                case 11:
                    return "removePasspointConfiguration";
                case 12:
                    return "getPasspointConfigurations";
                case 13:
                    return "getWifiConfigsForPasspointProfiles";
                case 14:
                    return "queryPasspointIcon";
                case 15:
                    return "matchProviderWithCurrentNetwork";
                case 16:
                    return "deauthenticateNetwork";
                case 17:
                    return "removeNetwork";
                case 18:
                    return "enableNetwork";
                case 19:
                    return "disableNetwork";
                case 20:
                    return "startScan";
                case 21:
                    return "getScanResults";
                case 22:
                    return "disconnect";
                case 23:
                    return "reconnect";
                case 24:
                    return "reassociate";
                case 25:
                    return "getConnectionInfo";
                case 26:
                    return "setWifiEnabled";
                case 27:
                    return "getWifiEnabledState";
                case 28:
                    return "setCountryCode";
                case 29:
                    return "getCountryCode";
                case 30:
                    return "isDualBandSupported";
                case 31:
                    return "needs5GHzToAnyApBandConversion";
                case 32:
                    return "getDhcpInfo";
                case 33:
                    return "isScanAlwaysAvailable";
                case 34:
                    return "acquireWifiLock";
                case 35:
                    return "updateWifiLockWorkSource";
                case 36:
                    return "releaseWifiLock";
                case 37:
                    return "initializeMulticastFiltering";
                case 38:
                    return "isMulticastEnabled";
                case 39:
                    return "acquireMulticastLock";
                case 40:
                    return "releaseMulticastLock";
                case 41:
                    return "updateInterfaceIpState";
                case 42:
                    return "startSoftAp";
                case 43:
                    return "stopSoftAp";
                case 44:
                    return "startLocalOnlyHotspot";
                case 45:
                    return "stopLocalOnlyHotspot";
                case 46:
                    return "startWatchLocalOnlyHotspot";
                case 47:
                    return "stopWatchLocalOnlyHotspot";
                case 48:
                    return "getWifiApEnabledState";
                case 49:
                    return "getWifiApConfiguration";
                case 50:
                    return "setWifiApConfiguration";
                case 51:
                    return "notifyUserOfApBandConversion";
                case 52:
                    return "getWifiServiceMessenger";
                case 53:
                    return "enableTdls";
                case 54:
                    return "enableTdlsWithMacAddress";
                case 55:
                    return "getCurrentNetworkWpsNfcConfigurationToken";
                case 56:
                    return "enableVerboseLogging";
                case 57:
                    return "getVerboseLoggingLevel";
                case 58:
                    return "enableWifiConnectivityManager";
                case 59:
                    return "disableEphemeralNetwork";
                case 60:
                    return "factoryReset";
                case 61:
                    return "getCurrentNetwork";
                case 62:
                    return "retrieveBackupData";
                case 63:
                    return "restoreBackupData";
                case 64:
                    return "restoreSupplicantBackupData";
                case 65:
                    return "startSubscriptionProvisioning";
                case 66:
                    return "registerSoftApCallback";
                case 67:
                    return "unregisterSoftApCallback";
                case 68:
                    return "addOnWifiUsabilityStatsListener";
                case 69:
                    return "removeOnWifiUsabilityStatsListener";
                case 70:
                    return "registerTrafficStateCallback";
                case 71:
                    return "unregisterTrafficStateCallback";
                case 72:
                    return "setSlaAppState";
                case 73:
                    return "getSlaAppState";
                case 74:
                    return "getAllSlaAppsAndStates";
                case 75:
                    return "getAllSlaAcceleratedApps";
                case 76:
                    return "isSlaSupported";
                case 77:
                    return "getAllDualStaApps";
                case 78:
                    return "isDualStaSupported";
                case 79:
                    return "getOppoSta2ConnectionInfo";
                case 80:
                    return "getOppoSta2CurConfigKey";
                case 81:
                    return "enableDualSta";
                case 82:
                    return "enableDualStaByForce";
                case 83:
                    return "disableDualSta";
                case 84:
                    return "registerNetworkRequestMatchCallback";
                case 85:
                    return "unregisterNetworkRequestMatchCallback";
                case 86:
                    return "addNetworkSuggestions";
                case 87:
                    return "removeNetworkSuggestions";
                case 88:
                    return "getFactoryMacAddresses";
                case 89:
                    return "setDeviceMobilityState";
                case 90:
                    return "startDppAsConfiguratorInitiator";
                case 91:
                    return "startDppAsEnrolleeInitiator";
                case 92:
                    return "stopDppSession";
                case 93:
                    return "updateWifiUsabilityScore";
                case 94:
                    return "getWifiPowerEventCode";
                case 95:
                    return "setNetworkCaptiveState";
                case 96:
                    return "getNetworkEverCaptiveState";
                case 97:
                    return "startApWps";
                case 98:
                    return "getHotspotClients";
                case 99:
                    return "getBlockedHotspotClients";
                case 100:
                    return "getClientIp";
                case 101:
                    return "getClientDeviceName";
                case 102:
                    return "blockClient";
                case 103:
                    return "unblockClient";
                case 104:
                    return "isAllDevicesAllowed";
                case 105:
                    return "setAllDevicesAllowed";
                case 106:
                    return "allowDevice";
                case 107:
                    return "disallowDevice";
                case 108:
                    return "getAllowedDevices";
                case 109:
                    return "startWifiSharing";
                case 110:
                    return "stopWifiSharing";
                case 111:
                    return "getWifiSharingConfiguration";
                case 112:
                    return "setWifiSharingConfiguration";
                case 113:
                    return "clearWifiOCloudData";
                case 114:
                    return "getWifiOCloudData";
                case 115:
                    return "updateGlobalId";
                case 116:
                    return "removeNetworkByGlobalId";
                case 117:
                    return "setDirtyFlag";
                case 118:
                    return "hasOCloudDirtyData";
                case 119:
                    return "isP2p5GSupported";
                case 120:
                    return "isSoftap5GSupported";
                case 121:
                    return "registerStaStateCallback";
                case 122:
                    return "unregisterStaStateCallback";
                case 123:
                    return "setPowerSavingMode";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ResultReceiver _arg0;
            WifiConfiguration _arg02;
            PasspointConfiguration _arg03;
            WorkSource _arg3;
            WorkSource _arg1;
            WifiConfiguration _arg04;
            Messenger _arg05;
            Messenger _arg06;
            WifiConfiguration _arg07;
            OsuProvider _arg08;
            WpsInfo _arg09;
            HotspotClient _arg010;
            HotspotClient _arg011;
            WifiConfiguration _arg012;
            WifiConfiguration _arg013;
            if (code != 1598968902) {
                boolean _arg12 = false;
                boolean _arg014 = false;
                boolean _arg13 = false;
                boolean _arg2 = false;
                boolean _arg015 = false;
                boolean _arg016 = false;
                boolean _arg14 = false;
                boolean _arg017 = false;
                boolean _arg018 = false;
                boolean _arg15 = false;
                boolean _arg019 = false;
                boolean _arg16 = false;
                boolean _arg17 = false;
                boolean _arg18 = false;
                boolean _arg19 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = getSupportedFeatures();
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        WifiActivityEnergyInfo _result2 = reportActivityInfo();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        requestActivityInfo(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result3 = getConfiguredNetworks(data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result4 = getPrivilegedConfiguredNetworks(data.readString());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result5 = getAllMatchingFqdnsForScanResults(data.createTypedArrayList(ScanResult.CREATOR));
                        reply.writeNoException();
                        reply.writeMap(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result6 = getMatchingOsuProviders(data.createTypedArrayList(ScanResult.CREATOR));
                        reply.writeNoException();
                        reply.writeMap(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result7 = getMatchingPasspointConfigsForOsuProviders(data.createTypedArrayList(OsuProvider.CREATOR));
                        reply.writeNoException();
                        reply.writeMap(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = WifiConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result8 = addOrUpdateNetwork(_arg02, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = PasspointConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean addOrUpdatePasspointConfiguration = addOrUpdatePasspointConfiguration(_arg03, data.readString());
                        reply.writeNoException();
                        reply.writeInt(addOrUpdatePasspointConfiguration ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removePasspointConfiguration = removePasspointConfiguration(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(removePasspointConfiguration ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<PasspointConfiguration> _result9 = getPasspointConfigurations(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result9);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        List<WifiConfiguration> _result10 = getWifiConfigsForPasspointProfiles(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeTypedList(_result10);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        queryPasspointIcon(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = matchProviderWithCurrentNetwork(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg020 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        deauthenticateNetwork(_arg020, _arg12);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeNetwork = removeNetwork(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeNetwork ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg021 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean enableNetwork = enableNetwork(_arg021, _arg19, data.readString());
                        reply.writeNoException();
                        reply.writeInt(enableNetwork ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableNetwork = disableNetwork(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(disableNetwork ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startScan = startScan(data.readString());
                        reply.writeNoException();
                        reply.writeInt(startScan ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        List<ScanResult> _result12 = getScanResults(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result12);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disconnect = disconnect(data.readString());
                        reply.writeNoException();
                        reply.writeInt(disconnect ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean reconnect = reconnect(data.readString());
                        reply.writeNoException();
                        reply.writeInt(reconnect ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean reassociate = reassociate(data.readString());
                        reply.writeNoException();
                        reply.writeInt(reassociate ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        WifiInfo _result13 = getConnectionInfo(data.readString());
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        boolean wifiEnabled = setWifiEnabled(_arg022, _arg18);
                        reply.writeNoException();
                        reply.writeInt(wifiEnabled ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getWifiEnabledState();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        setCountryCode(data.readString());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String _result15 = getCountryCode();
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDualBandSupported = isDualBandSupported();
                        reply.writeNoException();
                        reply.writeInt(isDualBandSupported ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean needs5GHzToAnyApBandConversion = needs5GHzToAnyApBandConversion();
                        reply.writeNoException();
                        reply.writeInt(needs5GHzToAnyApBandConversion ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        DhcpInfo _result16 = getDhcpInfo();
                        reply.writeNoException();
                        if (_result16 != null) {
                            reply.writeInt(1);
                            _result16.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScanAlwaysAvailable = isScanAlwaysAvailable();
                        reply.writeNoException();
                        reply.writeInt(isScanAlwaysAvailable ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg023 = data.readStrongBinder();
                        int _arg110 = data.readInt();
                        String _arg22 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        boolean acquireWifiLock = acquireWifiLock(_arg023, _arg110, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(acquireWifiLock ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg024 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        updateWifiLockWorkSource(_arg024, _arg1);
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean releaseWifiLock = releaseWifiLock(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(releaseWifiLock ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        initializeMulticastFiltering();
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isMulticastEnabled = isMulticastEnabled();
                        reply.writeNoException();
                        reply.writeInt(isMulticastEnabled ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        acquireMulticastLock(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        releaseMulticastLock(data.readString());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        updateInterfaceIpState(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = WifiConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean startSoftAp = startSoftAp(_arg04);
                        reply.writeNoException();
                        reply.writeInt(startSoftAp ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopSoftAp = stopSoftAp();
                        reply.writeNoException();
                        reply.writeInt(stopSoftAp ? 1 : 0);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        int _result17 = startLocalOnlyHotspot(_arg05, data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        stopLocalOnlyHotspot();
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        startWatchLocalOnlyHotspot(_arg06, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        stopWatchLocalOnlyHotspot();
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getWifiApEnabledState();
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        WifiConfiguration _result19 = getWifiApConfiguration();
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = WifiConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        boolean wifiApConfiguration = setWifiApConfiguration(_arg07, data.readString());
                        reply.writeNoException();
                        reply.writeInt(wifiApConfiguration ? 1 : 0);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        notifyUserOfApBandConversion(data.readString());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        Messenger _result20 = getWifiServiceMessenger(data.readString());
                        reply.writeNoException();
                        if (_result20 != null) {
                            reply.writeInt(1);
                            _result20.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg025 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = true;
                        }
                        enableTdls(_arg025, _arg17);
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg026 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        enableTdlsWithMacAddress(_arg026, _arg16);
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getCurrentNetworkWpsNfcConfigurationToken();
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        enableVerboseLogging(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = getVerboseLoggingLevel();
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = true;
                        }
                        enableWifiConnectivityManager(_arg019);
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        disableEphemeralNetwork(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        factoryReset(data.readString());
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        Network _result23 = getCurrentNetwork();
                        reply.writeNoException();
                        if (_result23 != null) {
                            reply.writeInt(1);
                            _result23.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result24 = retrieveBackupData();
                        reply.writeNoException();
                        reply.writeByteArray(_result24);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        restoreBackupData(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        restoreSupplicantBackupData(data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = OsuProvider.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        startSubscriptionProvisioning(_arg08, IProvisioningCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        registerSoftApCallback(data.readStrongBinder(), ISoftApCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterSoftApCallback(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        addOnWifiUsabilityStatsListener(data.readStrongBinder(), IOnWifiUsabilityStatsListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        removeOnWifiUsabilityStatsListener(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        registerTrafficStateCallback(data.readStrongBinder(), ITrafficStateCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterTrafficStateCallback(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg027 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean slaAppState = setSlaAppState(_arg027, _arg15);
                        reply.writeNoException();
                        reply.writeInt(slaAppState ? 1 : 0);
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        boolean slaAppState2 = getSlaAppState(data.readString());
                        reply.writeNoException();
                        reply.writeInt(slaAppState2 ? 1 : 0);
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result25 = getAllSlaAppsAndStates();
                        reply.writeNoException();
                        reply.writeStringArray(_result25);
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result26 = getAllSlaAcceleratedApps();
                        reply.writeNoException();
                        reply.writeStringArray(_result26);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSlaSupported = isSlaSupported();
                        reply.writeNoException();
                        reply.writeInt(isSlaSupported ? 1 : 0);
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result27 = getAllDualStaApps();
                        reply.writeNoException();
                        reply.writeStringArray(_result27);
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDualStaSupported = isDualStaSupported();
                        reply.writeNoException();
                        reply.writeInt(isDualStaSupported ? 1 : 0);
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        WifiInfo _result28 = getOppoSta2ConnectionInfo(data.readString());
                        reply.writeNoException();
                        if (_result28 != null) {
                            reply.writeInt(1);
                            _result28.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        String _result29 = getOppoSta2CurConfigKey();
                        reply.writeNoException();
                        reply.writeString(_result29);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = enableDualSta();
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = true;
                        }
                        int _result31 = enableDualStaByForce(_arg018);
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        disableDualSta();
                        reply.writeNoException();
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        registerNetworkRequestMatchCallback(data.readStrongBinder(), INetworkRequestMatchCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterNetworkRequestMatchCallback(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = addNetworkSuggestions(data.createTypedArrayList(WifiNetworkSuggestion.CREATOR), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        int _result33 = removeNetworkSuggestions(data.createTypedArrayList(WifiNetworkSuggestion.CREATOR), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result33);
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result34 = getFactoryMacAddresses();
                        reply.writeNoException();
                        reply.writeStringArray(_result34);
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        setDeviceMobilityState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        startDppAsConfiguratorInitiator(data.readStrongBinder(), data.readString(), data.readInt(), data.readInt(), IDppCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        startDppAsEnrolleeInitiator(data.readStrongBinder(), data.readString(), IDppCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        stopDppSession();
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        updateWifiUsabilityScore(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        String _result35 = getWifiPowerEventCode();
                        reply.writeNoException();
                        reply.writeString(_result35);
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = true;
                        }
                        setNetworkCaptiveState(_arg017);
                        reply.writeNoException();
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        boolean networkEverCaptiveState = getNetworkEverCaptiveState();
                        reply.writeNoException();
                        reply.writeInt(networkEverCaptiveState ? 1 : 0);
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = WpsInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        startApWps(_arg09);
                        reply.writeNoException();
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        List<HotspotClient> _result36 = getHotspotClients();
                        reply.writeNoException();
                        reply.writeTypedList(_result36);
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        List<HotspotClient> _result37 = getBlockedHotspotClients();
                        reply.writeNoException();
                        reply.writeTypedList(_result37);
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        String _result38 = getClientIp(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result38);
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        String _result39 = getClientDeviceName(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result39);
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = HotspotClient.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        boolean blockClient = blockClient(_arg010);
                        reply.writeNoException();
                        reply.writeInt(blockClient ? 1 : 0);
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = HotspotClient.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        boolean unblockClient = unblockClient(_arg011);
                        reply.writeNoException();
                        reply.writeInt(unblockClient ? 1 : 0);
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAllDevicesAllowed = isAllDevicesAllowed();
                        reply.writeNoException();
                        reply.writeInt(isAllDevicesAllowed ? 1 : 0);
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg028 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg14 = true;
                        }
                        boolean allDevicesAllowed = setAllDevicesAllowed(_arg028, _arg14);
                        reply.writeNoException();
                        reply.writeInt(allDevicesAllowed ? 1 : 0);
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        boolean allowDevice = allowDevice(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(allowDevice ? 1 : 0);
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disallowDevice = disallowDevice(data.readString());
                        reply.writeNoException();
                        reply.writeInt(disallowDevice ? 1 : 0);
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        List<HotspotClient> _result40 = getAllowedDevices();
                        reply.writeNoException();
                        reply.writeTypedList(_result40);
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = WifiConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        boolean startWifiSharing = startWifiSharing(_arg012);
                        reply.writeNoException();
                        reply.writeInt(startWifiSharing ? 1 : 0);
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopWifiSharing = stopWifiSharing();
                        reply.writeNoException();
                        reply.writeInt(stopWifiSharing ? 1 : 0);
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        WifiConfiguration _result41 = getWifiSharingConfiguration();
                        reply.writeNoException();
                        if (_result41 != null) {
                            reply.writeInt(1);
                            _result41.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = WifiConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg013 = null;
                        }
                        boolean wifiSharingConfiguration = setWifiSharingConfiguration(_arg013);
                        reply.writeNoException();
                        reply.writeInt(wifiSharingConfiguration ? 1 : 0);
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = true;
                        }
                        clearWifiOCloudData(_arg016);
                        reply.writeNoException();
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = true;
                        }
                        List<String> _result42 = getWifiOCloudData(_arg015);
                        reply.writeNoException();
                        reply.writeStringList(_result42);
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        updateGlobalId(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg029 = data.readString();
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        removeNetworkByGlobalId(_arg029, _arg111, _arg2);
                        reply.writeNoException();
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg030 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setDirtyFlag(_arg030, _arg13);
                        reply.writeNoException();
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasOCloudDirtyData = hasOCloudDirtyData();
                        reply.writeNoException();
                        reply.writeInt(hasOCloudDirtyData ? 1 : 0);
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isP2p5GSupported = isP2p5GSupported();
                        reply.writeNoException();
                        reply.writeInt(isP2p5GSupported ? 1 : 0);
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSoftap5GSupported = isSoftap5GSupported();
                        reply.writeNoException();
                        reply.writeInt(isSoftap5GSupported ? 1 : 0);
                        return true;
                    case 121:
                        data.enforceInterface(DESCRIPTOR);
                        registerStaStateCallback(data.readStrongBinder(), IStaStateCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 122:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterStaStateCallback(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 123:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = true;
                        }
                        setPowerSavingMode(_arg014);
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IWifiManager {
            public static IWifiManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.net.wifi.IWifiManager
            public long getSupportedFeatures() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedFeatures();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public WifiActivityEnergyInfo reportActivityInfo() throws RemoteException {
                WifiActivityEnergyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reportActivityInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiActivityEnergyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void requestActivityInfo(ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestActivityInfo(result);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public ParceledListSlice getConfiguredNetworks(String packageName) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfiguredNetworks(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public ParceledListSlice getPrivilegedConfiguredNetworks(String packageName) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrivilegedConfiguredNetworks(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public Map getAllMatchingFqdnsForScanResults(List<ScanResult> scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(scanResult);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllMatchingFqdnsForScanResults(scanResult);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public Map getMatchingOsuProviders(List<ScanResult> scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(scanResult);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMatchingOsuProviders(scanResult);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public Map getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> osuProviders) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(osuProviders);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMatchingPasspointConfigsForOsuProviders(osuProviders);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int addOrUpdateNetwork(WifiConfiguration config, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addOrUpdateNetwork(config, packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addOrUpdatePasspointConfiguration(config, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean removePasspointConfiguration(String fqdn, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fqdn);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removePasspointConfiguration(fqdn, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<PasspointConfiguration> getPasspointConfigurations(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPasspointConfigurations(packageName);
                    }
                    _reply.readException();
                    List<PasspointConfiguration> _result = _reply.createTypedArrayList(PasspointConfiguration.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> fqdnList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(fqdnList);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiConfigsForPasspointProfiles(fqdnList);
                    }
                    _reply.readException();
                    List<WifiConfiguration> _result = _reply.createTypedArrayList(WifiConfiguration.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void queryPasspointIcon(long bssid, String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(bssid);
                    _data.writeString(fileName);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().queryPasspointIcon(bssid, fileName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int matchProviderWithCurrentNetwork(String fqdn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fqdn);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchProviderWithCurrentNetwork(fqdn);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void deauthenticateNetwork(long holdoff, boolean ess) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(holdoff);
                    _data.writeInt(ess ? 1 : 0);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deauthenticateNetwork(holdoff, ess);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean removeNetwork(int netId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeNetwork(netId, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean enableNetwork(int netId, boolean disableOthers, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    boolean _result = true;
                    _data.writeInt(disableOthers ? 1 : 0);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableNetwork(netId, disableOthers, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean disableNetwork(int netId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableNetwork(netId, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean startScan(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startScan(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<ScanResult> getScanResults(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScanResults(callingPackage);
                    }
                    _reply.readException();
                    List<ScanResult> _result = _reply.createTypedArrayList(ScanResult.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean disconnect(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnect(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean reconnect(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reconnect(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean reassociate(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reassociate(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public WifiInfo getConnectionInfo(String callingPackage) throws RemoteException {
                WifiInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConnectionInfo(callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean setWifiEnabled(String packageName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiEnabled(packageName, enable);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int getWifiEnabledState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiEnabledState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void setCountryCode(String country) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(country);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCountryCode(country);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String getCountryCode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCountryCode();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isDualBandSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDualBandSupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean needs5GHzToAnyApBandConversion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().needs5GHzToAnyApBandConversion();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public DhcpInfo getDhcpInfo() throws RemoteException {
                DhcpInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDhcpInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = DhcpInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isScanAlwaysAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScanAlwaysAvailable();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean acquireWifiLock(IBinder lock, int lockType, String tag, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(lockType);
                    _data.writeString(tag);
                    boolean _result = true;
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acquireWifiLock(lock, lockType, tag, ws);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void updateWifiLockWorkSource(IBinder lock, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWifiLockWorkSource(lock, ws);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean releaseWifiLock(IBinder lock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().releaseWifiLock(lock);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void initializeMulticastFiltering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().initializeMulticastFiltering();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isMulticastEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMulticastEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void acquireMulticastLock(IBinder binder, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(tag);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().acquireMulticastLock(binder, tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void releaseMulticastLock(String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tag);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseMulticastLock(tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void updateInterfaceIpState(String ifaceName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifaceName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateInterfaceIpState(ifaceName, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean startSoftAp(WifiConfiguration wifiConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (wifiConfig != null) {
                        _data.writeInt(1);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startSoftAp(wifiConfig);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean stopSoftAp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopSoftAp();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startLocalOnlyHotspot(messenger, binder, packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void stopLocalOnlyHotspot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopLocalOnlyHotspot();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWatchLocalOnlyHotspot(messenger, binder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void stopWatchLocalOnlyHotspot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWatchLocalOnlyHotspot();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int getWifiApEnabledState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiApEnabledState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public WifiConfiguration getWifiApConfiguration() throws RemoteException {
                WifiConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiApConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean setWifiApConfiguration(WifiConfiguration wifiConfig, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (wifiConfig != null) {
                        _data.writeInt(1);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiApConfiguration(wifiConfig, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void notifyUserOfApBandConversion(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyUserOfApBandConversion(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public Messenger getWifiServiceMessenger(String packageName) throws RemoteException {
                Messenger _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiServiceMessenger(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void enableTdls(String remoteIPAddress, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteIPAddress);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableTdls(remoteIPAddress, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteMacAddress);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableTdlsWithMacAddress(remoteMacAddress, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String getCurrentNetworkWpsNfcConfigurationToken() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentNetworkWpsNfcConfigurationToken();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void enableVerboseLogging(int verbose) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(verbose);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableVerboseLogging(verbose);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int getVerboseLoggingLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVerboseLoggingLevel();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void enableWifiConnectivityManager(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableWifiConnectivityManager(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void disableEphemeralNetwork(String SSID, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(SSID);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableEphemeralNetwork(SSID, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void factoryReset(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().factoryReset(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public Network getCurrentNetwork() throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentNetwork();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public byte[] retrieveBackupData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().retrieveBackupData();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void restoreBackupData(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreBackupData(data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(supplicantData);
                    _data.writeByteArray(ipConfigData);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreSupplicantBackupData(supplicantData, ipConfigData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (provider != null) {
                        _data.writeInt(1);
                        provider.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startSubscriptionProvisioning(provider, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void registerSoftApCallback(IBinder binder, ISoftApCallback callback, int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerSoftApCallback(binder, callback, callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void unregisterSoftApCallback(int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterSoftApCallback(callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void addOnWifiUsabilityStatsListener(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(listenerIdentifier);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addOnWifiUsabilityStatsListener(binder, listener, listenerIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void removeOnWifiUsabilityStatsListener(int listenerIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(listenerIdentifier);
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeOnWifiUsabilityStatsListener(listenerIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void registerTrafficStateCallback(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerTrafficStateCallback(binder, callback, callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void unregisterTrafficStateCallback(int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterTrafficStateCallback(callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean setSlaAppState(String pkgName, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = true;
                    _data.writeInt(enabled ? 1 : 0);
                    if (!this.mRemote.transact(72, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSlaAppState(pkgName, enabled);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean getSlaAppState(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSlaAppState(pkgName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String[] getAllSlaAppsAndStates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllSlaAppsAndStates();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String[] getAllSlaAcceleratedApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllSlaAcceleratedApps();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isSlaSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSlaSupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String[] getAllDualStaApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllDualStaApps();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isDualStaSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(78, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDualStaSupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public WifiInfo getOppoSta2ConnectionInfo(String callingPackage) throws RemoteException {
                WifiInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(79, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOppoSta2ConnectionInfo(callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String getOppoSta2CurConfigKey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOppoSta2CurConfigKey();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int enableDualSta() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableDualSta();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int enableDualStaByForce(boolean force) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(force ? 1 : 0);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableDualStaByForce(force);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void disableDualSta() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(83, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableDualSta();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void registerNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(84, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerNetworkRequestMatchCallback(binder, callback, callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void unregisterNetworkRequestMatchCallback(int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(85, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterNetworkRequestMatchCallback(callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int addNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(networkSuggestions);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(86, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addNetworkSuggestions(networkSuggestions, packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public int removeNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(networkSuggestions);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeNetworkSuggestions(networkSuggestions, packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String[] getFactoryMacAddresses() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(88, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFactoryMacAddresses();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void setDeviceMobilityState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(89, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDeviceMobilityState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void startDppAsConfiguratorInitiator(IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(enrolleeUri);
                    _data.writeInt(selectedNetworkId);
                    _data.writeInt(netRole);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(90, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startDppAsConfiguratorInitiator(binder, enrolleeUri, selectedNetworkId, netRole, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void startDppAsEnrolleeInitiator(IBinder binder, String configuratorUri, IDppCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(configuratorUri);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startDppAsEnrolleeInitiator(binder, configuratorUri, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void stopDppSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopDppSession();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(seqNum);
                    _data.writeInt(score);
                    _data.writeInt(predictionHorizonSec);
                    if (this.mRemote.transact(93, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWifiUsabilityScore(seqNum, score, predictionHorizonSec);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String getWifiPowerEventCode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiPowerEventCode();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void setNetworkCaptiveState(boolean captiveState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(captiveState ? 1 : 0);
                    if (this.mRemote.transact(95, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetworkCaptiveState(captiveState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean getNetworkEverCaptiveState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(96, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkEverCaptiveState();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void startApWps(WpsInfo config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(97, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startApWps(config);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<HotspotClient> getHotspotClients() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(98, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHotspotClients();
                    }
                    _reply.readException();
                    List<HotspotClient> _result = _reply.createTypedArrayList(HotspotClient.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<HotspotClient> getBlockedHotspotClients() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(99, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBlockedHotspotClients();
                    }
                    _reply.readException();
                    List<HotspotClient> _result = _reply.createTypedArrayList(HotspotClient.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String getClientIp(String deviceAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceAddress);
                    if (!this.mRemote.transact(100, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getClientIp(deviceAddress);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public String getClientDeviceName(String deviceAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceAddress);
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getClientDeviceName(deviceAddress);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean blockClient(HotspotClient client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (client != null) {
                        _data.writeInt(1);
                        client.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(102, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().blockClient(client);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean unblockClient(HotspotClient client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (client != null) {
                        _data.writeInt(1);
                        client.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unblockClient(client);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isAllDevicesAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(104, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAllDevicesAllowed();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean setAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeInt(allowAllConnectedDevices ? 1 : 0);
                    if (!this.mRemote.transact(105, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAllDevicesAllowed(enabled, allowAllConnectedDevices);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean allowDevice(String deviceAddress, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceAddress);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(106, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().allowDevice(deviceAddress, name);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean disallowDevice(String deviceAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceAddress);
                    boolean _result = false;
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disallowDevice(deviceAddress);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<HotspotClient> getAllowedDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(108, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllowedDevices();
                    }
                    _reply.readException();
                    List<HotspotClient> _result = _reply.createTypedArrayList(HotspotClient.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean startWifiSharing(WifiConfiguration wifiConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (wifiConfig != null) {
                        _data.writeInt(1);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startWifiSharing(wifiConfig);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean stopWifiSharing() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(110, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopWifiSharing();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public WifiConfiguration getWifiSharingConfiguration() throws RemoteException {
                WifiConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(111, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiSharingConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean setWifiSharingConfiguration(WifiConfiguration wifiConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (wifiConfig != null) {
                        _data.writeInt(1);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(112, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiSharingConfiguration(wifiConfig);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void clearWifiOCloudData(boolean hardDelete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hardDelete ? 1 : 0);
                    if (this.mRemote.transact(113, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearWifiOCloudData(hardDelete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public List<String> getWifiOCloudData(boolean isDirtyOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isDirtyOnly ? 1 : 0);
                    if (!this.mRemote.transact(114, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiOCloudData(isDirtyOnly);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void updateGlobalId(int itemId, String globalId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(itemId);
                    _data.writeString(globalId);
                    if (this.mRemote.transact(115, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateGlobalId(itemId, globalId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void removeNetworkByGlobalId(String configKey, String globalId, boolean hardDelete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(configKey);
                    _data.writeString(globalId);
                    _data.writeInt(hardDelete ? 1 : 0);
                    if (this.mRemote.transact(116, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeNetworkByGlobalId(configKey, globalId, hardDelete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void setDirtyFlag(String globalId, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(globalId);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(117, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDirtyFlag(globalId, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean hasOCloudDirtyData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(118, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasOCloudDirtyData();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isP2p5GSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(119, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isP2p5GSupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public boolean isSoftap5GSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(120, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSoftap5GSupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void registerStaStateCallback(IBinder binder, IStaStateCallback callback, int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(121, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerStaStateCallback(binder, callback, callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void unregisterStaStateCallback(int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callbackIdentifier);
                    if (this.mRemote.transact(122, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterStaStateCallback(callbackIdentifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiManager
            public void setPowerSavingMode(boolean mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode ? 1 : 0);
                    if (this.mRemote.transact(123, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPowerSavingMode(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

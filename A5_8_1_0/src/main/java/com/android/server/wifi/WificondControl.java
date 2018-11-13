package com.android.server.wifi;

import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.IPnoScanEvent;
import android.net.wifi.IPnoScanEvent.Stub;
import android.net.wifi.IScanEvent;
import android.net.wifi.IWifiScannerImpl;
import android.net.wifi.IWificond;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiSsid;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.WifiNative.PnoNetwork;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.SignalPollResult;
import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.InformationElementUtil.Capabilities;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.wificond.ChannelSettings;
import com.android.server.wifi.wificond.HiddenNetwork;
import com.android.server.wifi.wificond.NativeScanResult;
import com.android.server.wifi.wificond.SingleScanSettings;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class WificondControl {
    private static final int MAX_SSID_LEN = 32;
    public static final int SCAN_TYPE_PNO_SCAN = 1;
    public static final int SCAN_TYPE_SINGLE_SCAN = 0;
    private static final String TAG = "WificondControl";
    private IApInterface mApInterface;
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private IClientInterface mClientInterface;
    private String mClientInterfaceName;
    private IPnoScanEvent mPnoScanEventHandler;
    private IScanEvent mScanEventHandler;
    private boolean mVerboseLoggingEnabled = false;
    private WifiInjector mWifiInjector;
    private WifiMonitor mWifiMonitor;
    private IWificond mWificond;
    private IWifiScannerImpl mWificondScanner;

    private class PnoScanEventHandler extends Stub {
        /* synthetic */ PnoScanEventHandler(WificondControl this$0, PnoScanEventHandler -this1) {
            this();
        }

        private PnoScanEventHandler() {
        }

        public void OnPnoNetworkFound() {
            Log.d(WificondControl.TAG, "Pno scan result event");
            WificondControl.this.mWifiMonitor.broadcastPnoScanResultEvent(WificondControl.this.mClientInterfaceName);
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoFoundNetworkEventCount();
        }

        public void OnPnoScanFailed() {
            Log.d(WificondControl.TAG, "Pno Scan failed event");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedCount();
        }

        public void OnPnoScanOverOffloadStarted() {
            Log.d(WificondControl.TAG, "Pno scan over offload started");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanStartedOverOffloadCount();
        }

        public void OnPnoScanOverOffloadFailed(int reason) {
            Log.d(WificondControl.TAG, "Pno scan over offload failed");
            WificondControl.this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedOverOffloadCount();
        }
    }

    private class ScanEventHandler extends IScanEvent.Stub {
        /* synthetic */ ScanEventHandler(WificondControl this$0, ScanEventHandler -this1) {
            this();
        }

        private ScanEventHandler() {
        }

        public void OnScanResultReady() {
            Log.d(WificondControl.TAG, "Scan result ready event");
            WificondControl.this.mWifiMonitor.broadcastScanResultEvent(WificondControl.this.mClientInterfaceName);
        }

        public void OnScanFailed() {
            Log.d(WificondControl.TAG, "Scan failed event");
            WificondControl.this.mWifiMonitor.broadcastScanFailedEvent(WificondControl.this.mClientInterfaceName);
        }
    }

    WificondControl(WifiInjector wifiInjector, WifiMonitor wifiMonitor, CarrierNetworkConfig carrierNetworkConfig) {
        this.mWifiInjector = wifiInjector;
        this.mWifiMonitor = wifiMonitor;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public IClientInterface setupDriverForClientMode() {
        Log.d(TAG, "Setting up driver for client mode");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return null;
        }
        try {
            IClientInterface clientInterface = this.mWificond.createClientInterface();
            if (clientInterface == null) {
                Log.e(TAG, "Could not get IClientInterface instance from wificond");
                return null;
            }
            Binder.allowBlocking(clientInterface.asBinder());
            this.mClientInterface = clientInterface;
            try {
                this.mClientInterfaceName = clientInterface.getInterfaceName();
                this.mWificondScanner = this.mClientInterface.getWifiScannerImpl();
                if (this.mWificondScanner == null) {
                    Log.e(TAG, "Failed to get WificondScannerImpl");
                    return null;
                }
                Binder.allowBlocking(this.mWificondScanner.asBinder());
                this.mScanEventHandler = new ScanEventHandler(this, null);
                this.mWificondScanner.subscribeScanEvents(this.mScanEventHandler);
                this.mPnoScanEventHandler = new PnoScanEventHandler(this, null);
                this.mWificondScanner.subscribePnoScanEvents(this.mPnoScanEventHandler);
                return clientInterface;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to refresh wificond scanner due to remote exception");
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get IClientInterface due to remote exception");
            return null;
        }
    }

    public IApInterface setupDriverForSoftApMode() {
        Log.d(TAG, "Setting up driver for soft ap mode");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return null;
        }
        try {
            IApInterface apInterface = this.mWificond.createApInterface();
            if (apInterface == null) {
                Log.e(TAG, "Could not get IApInterface instance from wificond");
                return null;
            }
            Binder.allowBlocking(apInterface.asBinder());
            this.mApInterface = apInterface;
            return apInterface;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get IApInterface due to remote exception");
            return null;
        }
    }

    public IApInterface QcSetupDriverForSoftApMode(boolean isDualMode) {
        String sapInterfaceName;
        if (isDualMode) {
            sapInterfaceName = this.mWifiInjector.getWifiApConfigStore().getBridgeInterface();
        } else {
            sapInterfaceName = this.mWifiInjector.getWifiApConfigStore().getSapInterface();
        }
        if (sapInterfaceName == null) {
            Log.d(TAG, "Can't setup SAP mode without interface names");
            return null;
        }
        Log.d(TAG, "Setting up driver for soft ap mode");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return null;
        }
        String[] dualSapIfname = null;
        if (isDualMode) {
            dualSapIfname = this.mWifiInjector.getWifiApConfigStore().getDualSapInterfaces();
        }
        IApInterface dualSapInterface1 = null;
        IApInterface dualSapInterface2 = null;
        if (isDualMode && dualSapIfname != null) {
            try {
                if (dualSapIfname.length == 2) {
                    dualSapInterface1 = this.mWificond.QcCreateApInterface(dualSapIfname[0].getBytes(StandardCharsets.UTF_8));
                    dualSapInterface2 = this.mWificond.QcCreateApInterface(dualSapIfname[1].getBytes(StandardCharsets.UTF_8));
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to get IApInterface due to remote exception");
                return null;
            }
        }
        IApInterface softApInterface = this.mWificond.QcCreateApInterface(sapInterfaceName.getBytes(StandardCharsets.UTF_8));
        if (softApInterface == null || (dualSapIfname != null && (dualSapInterface1 == null || dualSapInterface2 == null))) {
            Log.e(TAG, "Could not get IApInterface instance from wificond");
            return null;
        }
        Binder.allowBlocking(softApInterface.asBinder());
        this.mApInterface = softApInterface;
        return softApInterface;
    }

    public boolean tearDownInterfaces() {
        Log.d(TAG, "tearing down interfaces in wificond");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return false;
        }
        try {
            synchronized (this) {
                if (this.mWificondScanner != null) {
                    this.mWificondScanner.unsubscribeScanEvents();
                    this.mWificondScanner.unsubscribePnoScanEvents();
                }
                this.mWificond.tearDownInterfaces();
                this.mClientInterface = null;
                this.mWificondScanner = null;
                this.mPnoScanEventHandler = null;
                this.mScanEventHandler = null;
                this.mApInterface = null;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to tear down interfaces due to remote exception");
            return false;
        }
    }

    public boolean tearDownStaInterfaces() {
        Log.d(TAG, "tearing down STA interfaces in wificond");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return false;
        }
        try {
            this.mWificond.tearDownStaInterfaces();
            this.mClientInterface = null;
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to tear down STA interfaces due to remote exception");
            return false;
        }
    }

    public boolean tearDownApInterfaces() {
        Log.d(TAG, "tearing down AP interfaces in wificond");
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return false;
        }
        try {
            this.mWificond.tearDownApInterfaces();
            this.mApInterface = null;
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to tear down STA interfaces due to remote exception");
            return false;
        }
    }

    public boolean runQsapCmd(String cmd) {
        Log.d(TAG, "sending qsap cmd = " + cmd);
        this.mWificond = this.mWifiInjector.makeWificond();
        if (this.mWificond == null) {
            Log.e(TAG, "Failed to get reference to wificond");
            return false;
        }
        try {
            if (this.mWificond.setHostapdParam(cmd.getBytes(StandardCharsets.UTF_8))) {
                return true;
            }
            Log.e(TAG, "Failed to run qsap command");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to run qsap command due to remote exception");
            return false;
        }
    }

    public boolean disableSupplicant() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return false;
        }
        try {
            return this.mClientInterface.disableSupplicant();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disable supplicant due to remote exception");
            return false;
        }
    }

    public boolean enableSupplicant() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return false;
        }
        try {
            return this.mClientInterface.enableSupplicant();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to enable supplicant due to remote exception");
            return false;
        }
    }

    public SignalPollResult signalPoll() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = this.mClientInterface.signalPoll();
            if (resultArray == null || resultArray.length != 3) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            SignalPollResult pollResult = new SignalPollResult();
            pollResult.currentRssi = resultArray[0];
            pollResult.txBitrate = resultArray[1];
            pollResult.associationFrequency = resultArray[2];
            return pollResult;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to do signal polling due to remote exception");
            return null;
        }
    }

    public TxPacketCounters getTxPacketCounters() {
        if (this.mClientInterface == null) {
            Log.e(TAG, "No valid wificond client interface handler");
            return null;
        }
        try {
            int[] resultArray = this.mClientInterface.getPacketCounters();
            if (resultArray == null || resultArray.length != 2) {
                Log.e(TAG, "Invalid signal poll result from wificond");
                return null;
            }
            TxPacketCounters counters = new TxPacketCounters();
            counters.txSucceeded = resultArray[0];
            counters.txFailed = resultArray[1];
            return counters;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to do signal polling due to remote exception");
            return null;
        }
    }

    public ArrayList<ScanDetail> getScanResults(int scanType) {
        ArrayList<ScanDetail> results = new ArrayList();
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return results;
        }
        NativeScanResult[] nativeResults;
        if (scanType == 0) {
            try {
                nativeResults = this.mWificondScanner.getScanResults();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to create ScanDetail ArrayList");
            }
        } else {
            nativeResults = this.mWificondScanner.getPnoScanResults();
        }
        int i = 0;
        int length = nativeResults.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                break;
            }
            NativeScanResult result = nativeResults[i2];
            WifiSsid wifiSsid = WifiSsid.createFromByteArray(result.ssid);
            try {
                String bssid = NativeUtil.macAddressFromByteArray(result.bssid);
                if (bssid == null) {
                    Log.e(TAG, "Illegal null bssid");
                    i = i2 + 1;
                } else {
                    InformationElement[] ies = InformationElementUtil.parseInformationElements(result.infoElement);
                    Capabilities capabilities = new Capabilities();
                    capabilities.from(ies, result.capability);
                    String flags = capabilities.generateCapabilitiesString();
                    try {
                        NetworkDetail networkDetail = new NetworkDetail(bssid, ies, null, result.frequency);
                        int level = result.signalMbm / 100;
                        if (level >= -93) {
                            ScanDetail scanDetail = new ScanDetail(networkDetail, wifiSsid, bssid, flags, result.signalMbm / 100, result.frequency, result.tsf, ies, null);
                            if (ScanResultUtil.isScanResultForEapNetwork(scanDetail.getScanResult()) && this.mCarrierNetworkConfig.isCarrierNetwork(wifiSsid.toString())) {
                                scanDetail.getScanResult().isCarrierAp = true;
                                scanDetail.getScanResult().carrierApEapType = this.mCarrierNetworkConfig.getNetworkEapType(wifiSsid.toString());
                                scanDetail.getScanResult().carrierName = this.mCarrierNetworkConfig.getCarrierName(wifiSsid.toString());
                            }
                            results.add(scanDetail);
                        } else if (this.mVerboseLoggingEnabled) {
                            Log.d(TAG, "Remove poor signal APs, SSID=" + wifiSsid + ", bssid=" + bssid + ", freq=" + result.frequency + ", level=" + level + ", flags=" + flags);
                        }
                    } catch (IllegalArgumentException e2) {
                        Log.e(TAG, "Illegal argument for scan result with bssid: " + bssid, e2);
                    }
                    i = i2 + 1;
                }
            } catch (IllegalArgumentException e22) {
                Log.e(TAG, "Illegal argument " + result.bssid, e22);
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "get " + results.size() + " scan results from wificond");
        }
        return results;
    }

    public ArrayList<Byte> getWifiGbkHistory(ArrayList<Byte> ssid) {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return ssid;
        }
        try {
            byte[] ssid_array = NativeUtil.byteArrayFromArrayList(ssid);
            byte[] out_ssid_array = this.mWificondScanner.getWifiGbkHistory(ssid_array);
            if (out_ssid_array != null && out_ssid_array.length > 0) {
                ssid = NativeUtil.byteArrayToArrayList(out_ssid_array);
                Log.d(TAG, "getWifiGbkHistory success - ssid= " + NativeUtil.hexStringFromByteArray(ssid_array) + " out_ssid=" + NativeUtil.hexStringFromByteArray(out_ssid_array));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to getWifiGbkHistory! (due to RemoteException)");
        }
        return ssid;
    }

    public boolean scan(Set<Integer> freqs, Set<String> hiddenNetworkSSIDs) {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        SingleScanSettings settings = new SingleScanSettings();
        settings.channelSettings = new ArrayList();
        settings.hiddenNetworks = new ArrayList();
        if (freqs != null) {
            for (Integer freq : freqs) {
                ChannelSettings channel = new ChannelSettings();
                channel.frequency = freq.intValue();
                settings.channelSettings.add(channel);
            }
        }
        if (hiddenNetworkSSIDs != null) {
            for (String ssid : hiddenNetworkSSIDs) {
                HiddenNetwork network = new HiddenNetwork();
                try {
                    ArrayList<Byte> ssid_l = NativeUtil.decodeSsid(ssid);
                    if (!NativeUtil.isAllAscii(ssid_l)) {
                        byte[] ssid2 = NativeUtil.getSsidBytes(ssid, "GBK");
                        if (ssid2 != null && ssid2.length > 0) {
                            HiddenNetwork network2 = new HiddenNetwork();
                            network2.ssid = ssid2;
                            String hexS = NativeUtil.hexStringFromByteArray(network2.ssid);
                            Log.i(TAG, "scan - extra Gbk hidden_ssid=" + hexS);
                            if (network2.ssid.length > 32) {
                                Log.e(TAG, "scan - skip long ssid = " + hexS);
                            } else {
                                settings.hiddenNetworks.add(network2);
                            }
                        }
                    }
                    network.ssid = NativeUtil.byteArrayFromArrayList(ssid_l);
                    if (network.ssid.length > 32) {
                        Log.e(TAG, "SSID is too long after conversion, skipping this ssid! SSID = " + network.ssid + " , network.ssid.size = " + network.ssid.length);
                    } else {
                        settings.hiddenNetworks.add(network);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + ssid, e);
                }
            }
        }
        try {
            return this.mWificondScanner.scan(settings);
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to request scan due to remote exception");
            return false;
        }
    }

    public boolean startPnoScan(PnoSettings pnoSettings) {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        com.android.server.wifi.wificond.PnoSettings settings = new com.android.server.wifi.wificond.PnoSettings();
        settings.pnoNetworks = new ArrayList();
        settings.intervalMs = pnoSettings.periodInMs;
        settings.min2gRssi = pnoSettings.min24GHzRssi;
        settings.min5gRssi = pnoSettings.min5GHzRssi;
        if (pnoSettings.networkList != null) {
            for (PnoNetwork network : pnoSettings.networkList) {
                com.android.server.wifi.wificond.PnoNetwork condNetwork = new com.android.server.wifi.wificond.PnoNetwork();
                condNetwork.isHidden = (network.flags & 1) != 0;
                try {
                    ArrayList<Byte> ssid_l = NativeUtil.decodeSsid(network.ssid);
                    if (!NativeUtil.isAllAscii(ssid_l)) {
                        ArrayList<Byte> out_ssid_l = getWifiGbkHistory(ssid_l);
                        if (!(out_ssid_l == null || (out_ssid_l.equals(ssid_l) ^ 1) == 0)) {
                            ssid_l = out_ssid_l;
                            Log.i(TAG, "startPnoScan - Gbk hidden_ssid=" + NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(out_ssid_l)));
                        }
                    }
                    condNetwork.ssid = NativeUtil.byteArrayFromArrayList(ssid_l);
                    if (condNetwork.ssid.length > 32) {
                        Log.e(TAG, "startPnoScan - drop too long ssid=" + NativeUtil.hexStringFromByteArray(condNetwork.ssid));
                    } else {
                        settings.pnoNetworks.add(condNetwork);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal argument " + network.ssid, e);
                }
            }
        }
        try {
            boolean success = this.mWificondScanner.startPnoScan(settings);
            this.mWifiInjector.getWifiMetrics().incrementPnoScanStartAttempCount();
            if (!success) {
                this.mWifiInjector.getWifiMetrics().incrementPnoScanFailedCount();
            }
            return success;
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to start pno scan due to remote exception");
            return false;
        }
    }

    public boolean stopPnoScan() {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return false;
        }
        try {
            return this.mWificondScanner.stopPnoScan();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to stop pno scan due to remote exception");
            return false;
        }
    }

    public void abortScan() {
        if (this.mWificondScanner == null) {
            Log.e(TAG, "No valid wificond scanner interface handler");
            return;
        }
        try {
            this.mWificondScanner.abortScan();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to request abortScan due to remote exception");
        }
    }
}

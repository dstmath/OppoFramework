package com.android.server.wifi;

import android.net.apf.ApfCapabilities;
import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.RttManager.ResponderConfig;
import android.net.wifi.RttManager.RttCapabilities;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.RttManager.RttResult;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiSsid;
import android.net.wifi.WifiWakeReasonAndCounts;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.Immutable;
import com.android.internal.util.HexDump;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.util.FrameParser;
import com.android.server.wifi.util.NativeUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class WifiNative {
    public static final int BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    public static final int BLUETOOTH_COEXISTENCE_MODE_ENABLED = 0;
    public static final int BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    public static final int DISABLE_FIRMWARE_ROAMING = 0;
    public static final int ENABLE_FIRMWARE_ROAMING = 1;
    public static final int RX_FILTER_TYPE_V4_MULTICAST = 0;
    public static final int RX_FILTER_TYPE_V6_MULTICAST = 1;
    public static final int SETUP_FAILURE_HAL = 1;
    public static final int SETUP_FAILURE_WIFICOND = 2;
    public static final int SETUP_SUCCESS = 0;
    public static final String SIM_AUTH_RESP_TYPE_GSM_AUTH = "GSM-AUTH";
    public static final String SIM_AUTH_RESP_TYPE_UMTS_AUTH = "UMTS-AUTH";
    public static final String SIM_AUTH_RESP_TYPE_UMTS_AUTS = "UMTS-AUTS";
    public static final int TX_POWER_SCENARIO_NORMAL = 0;
    public static final int TX_POWER_SCENARIO_VOICE_CALL = 1;
    public static final int WIFI_SCAN_FAILED = 3;
    public static final int WIFI_SCAN_RESULTS_AVAILABLE = 0;
    public static final int WIFI_SCAN_THRESHOLD_NUM_SCANS = 1;
    public static final int WIFI_SCAN_THRESHOLD_PERCENT = 2;
    private final String mInterfaceName;
    private boolean mStaAndAPConcurrency = false;
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private final String mTAG;
    private final WifiInjector mWifiInjector;
    private final WifiVendorHal mWifiVendorHal;
    private final WificondControl mWificondControl;

    public interface VendorHalDeathEventHandler {
        void onDeath();
    }

    public interface RttEventHandler {
        void onRttResults(RttResult[] rttResultArr);
    }

    public interface WifiLoggerEventHandler {
        void onRingBufferData(RingBufferStatus ringBufferStatus, byte[] bArr);

        void onWifiAlert(int i, byte[] bArr);
    }

    public static class BucketSettings {
        public int band;
        public int bucket;
        public ChannelSettings[] channels;
        public int max_period_ms;
        public int num_channels;
        public int period_ms;
        public int report_events;
        public int step_count;
    }

    public static class ChannelSettings {
        public int dwell_time_ms;
        public int frequency;
        public boolean passive;
    }

    @Immutable
    static abstract class FateReport {
        static final int MAX_DRIVER_TIMESTAMP_MSEC = 4294967;
        static final int USEC_PER_MSEC = 1000;
        static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
        final long mDriverTimestampUSec;
        final long mEstimatedWallclockMSec = convertDriverTimestampUSecToWallclockMSec(this.mDriverTimestampUSec);
        final byte mFate;
        final byte[] mFrameBytes;
        final byte mFrameType;

        protected abstract String directionToString();

        protected abstract String fateToString();

        FateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            this.mFate = fate;
            this.mDriverTimestampUSec = driverTimestampUSec;
            this.mFrameType = frameType;
            this.mFrameBytes = frameBytes;
        }

        public String toTableRowString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            pw.format("%-15s  %12s  %-9s  %-32s  %-12s  %-23s  %s\n", new Object[]{Long.valueOf(this.mDriverTimestampUSec), dateFormatter.format(new Date(this.mEstimatedWallclockMSec)), directionToString(), fateToString(), parser.mMostSpecificProtocolString, parser.mTypeString, parser.mResultString});
            return sw.toString();
        }

        public String toVerboseStringWithPiiAllowed() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            pw.format("Frame direction: %s\n", new Object[]{directionToString()});
            pw.format("Frame timestamp: %d\n", new Object[]{Long.valueOf(this.mDriverTimestampUSec)});
            pw.format("Frame fate: %s\n", new Object[]{fateToString()});
            pw.format("Frame type: %s\n", new Object[]{frameTypeToString(this.mFrameType)});
            pw.format("Frame protocol: %s\n", new Object[]{parser.mMostSpecificProtocolString});
            pw.format("Frame protocol type: %s\n", new Object[]{parser.mTypeString});
            pw.format("Frame length: %d\n", new Object[]{Integer.valueOf(this.mFrameBytes.length)});
            pw.append("Frame bytes");
            pw.append(HexDump.dumpHexString(this.mFrameBytes));
            pw.append("\n");
            return sw.toString();
        }

        public static String getTableHeader() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.format("\n%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", new Object[]{"Time usec", "Walltime", "Direction", "Fate", "Protocol", "Type", "Result"});
            pw.format("%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", new Object[]{"---------", "--------", "---------", "----", "--------", "----", "------"});
            return sw.toString();
        }

        private static String frameTypeToString(byte frameType) {
            switch (frameType) {
                case (byte) 0:
                    return "unknown";
                case (byte) 1:
                    return "data";
                case (byte) 2:
                    return "802.11 management";
                default:
                    return Byte.toString(frameType);
            }
        }

        private static long convertDriverTimestampUSecToWallclockMSec(long driverTimestampUSec) {
            long wallclockMillisNow = System.currentTimeMillis();
            long driverTimestampMillis = driverTimestampUSec / 1000;
            long boottimeTimestampMillis = SystemClock.elapsedRealtime() % 4294967;
            if (boottimeTimestampMillis < driverTimestampMillis) {
                boottimeTimestampMillis += 4294967;
            }
            return wallclockMillisNow - (boottimeTimestampMillis - driverTimestampMillis);
        }
    }

    public static class HiddenNetwork {
        public String ssid;

        public boolean equals(Object otherObj) {
            if (this == otherObj) {
                return true;
            }
            if (otherObj == null || getClass() != otherObj.getClass()) {
                return false;
            }
            return Objects.equals(this.ssid, ((HiddenNetwork) otherObj).ssid);
        }

        public int hashCode() {
            return this.ssid == null ? 0 : this.ssid.hashCode();
        }
    }

    public interface PnoEventHandler {
        void onPnoNetworkFound(ScanResult[] scanResultArr);

        void onPnoScanFailed();
    }

    public static class PnoNetwork {
        public byte auth_bit_field;
        public byte flags;
        public String ssid;

        public boolean equals(Object otherObj) {
            boolean z = true;
            if (this == otherObj) {
                return true;
            }
            if (otherObj == null || getClass() != otherObj.getClass()) {
                return false;
            }
            PnoNetwork other = (PnoNetwork) otherObj;
            if (!Objects.equals(this.ssid, other.ssid) || this.flags != other.flags) {
                z = false;
            } else if (this.auth_bit_field != other.auth_bit_field) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (this.ssid == null ? 0 : this.ssid.hashCode()) ^ ((this.flags * 31) + (this.auth_bit_field << 8));
        }
    }

    public static class PnoSettings {
        public int band5GHzBonus;
        public int currentConnectionBonus;
        public int initialScoreMax;
        public boolean isConnected;
        public int min24GHzRssi;
        public int min5GHzRssi;
        public PnoNetwork[] networkList;
        public int periodInMs;
        public int sameNetworkBonus;
        public int secureBonus;
    }

    public static class RingBufferStatus {
        public static final int HAS_ASCII_ENTRIES = 2;
        public static final int HAS_BINARY_ENTRIES = 1;
        public static final int HAS_PER_PACKET_ENTRIES = 4;
        int flag;
        String name;
        int readBytes;
        int ringBufferByteSize;
        int ringBufferId;
        int verboseLevel;
        int writtenBytes;
        int writtenRecords;

        public String toString() {
            return "name: " + this.name + " flag: " + this.flag + " ringBufferId: " + this.ringBufferId + " ringBufferByteSize: " + this.ringBufferByteSize + " verboseLevel: " + this.verboseLevel + " writtenBytes: " + this.writtenBytes + " readBytes: " + this.readBytes + " writtenRecords: " + this.writtenRecords;
        }
    }

    public static class RoamingCapabilities {
        public int maxBlacklistSize;
        public int maxWhitelistSize;
    }

    public static class RoamingConfig {
        public ArrayList<String> blacklistBssids;
        public ArrayList<String> whitelistSsids;
    }

    @Immutable
    public static final class RxFateReport extends FateReport {
        RxFateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            super(fate, driverTimestampUSec, frameType, frameBytes);
        }

        protected String directionToString() {
            return "RX";
        }

        protected String fateToString() {
            switch (this.mFate) {
                case (byte) 0:
                    return "success";
                case (byte) 1:
                    return "firmware queued";
                case (byte) 2:
                    return "firmware dropped (filter)";
                case (byte) 3:
                    return "firmware dropped (invalid frame)";
                case (byte) 4:
                    return "firmware dropped (no bufs)";
                case (byte) 5:
                    return "firmware dropped (other)";
                case (byte) 6:
                    return "driver queued";
                case (byte) 7:
                    return "driver dropped (filter)";
                case (byte) 8:
                    return "driver dropped (invalid frame)";
                case (byte) 9:
                    return "driver dropped (no bufs)";
                case (byte) 10:
                    return "driver dropped (other)";
                default:
                    return Byte.toString(this.mFate);
            }
        }
    }

    public static class ScanCapabilities {
        public int max_ap_cache_per_scan;
        public int max_rssi_sample_size;
        public int max_scan_buckets;
        public int max_scan_cache_size;
        public int max_scan_reporting_threshold;
    }

    public interface ScanEventHandler {
        void onFullScanResult(ScanResult scanResult, int i);

        void onScanPaused(ScanData[] scanDataArr);

        void onScanRestarted();

        void onScanStatus(int i);
    }

    public static class ScanSettings {
        public int base_period_ms;
        public BucketSettings[] buckets;
        public HiddenNetwork[] hiddenNetworks;
        public int max_ap_per_scan;
        public int num_buckets;
        public int report_threshold_num_scans;
        public int report_threshold_percent;
    }

    public static class SignalPollResult {
        public int associationFrequency;
        public int currentRssi;
        public int txBitrate;
    }

    @Immutable
    public static final class TxFateReport extends FateReport {
        TxFateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            super(fate, driverTimestampUSec, frameType, frameBytes);
        }

        protected String directionToString() {
            return "TX";
        }

        protected String fateToString() {
            switch (this.mFate) {
                case (byte) 0:
                    return "acked";
                case (byte) 1:
                    return "sent";
                case (byte) 2:
                    return "firmware queued";
                case (byte) 3:
                    return "firmware dropped (invalid frame)";
                case (byte) 4:
                    return "firmware dropped (no bufs)";
                case (byte) 5:
                    return "firmware dropped (other)";
                case (byte) 6:
                    return "driver queued";
                case (byte) 7:
                    return "driver dropped (invalid frame)";
                case (byte) 8:
                    return "driver dropped (no bufs)";
                case (byte) 9:
                    return "driver dropped (other)";
                default:
                    return Byte.toString(this.mFate);
            }
        }
    }

    public static class TxPacketCounters {
        public int txFailed;
        public int txSucceeded;
    }

    public interface WifiRssiEventHandler {
        void onRssiThresholdBreached(byte b);
    }

    private static native byte[] readKernelLogNative();

    private static native int registerNatives();

    public WifiNative(String interfaceName, WifiVendorHal vendorHal, SupplicantStaIfaceHal staIfaceHal, WificondControl condControl, WifiInjector wifiInjector) {
        this.mTAG = "WifiNative-" + interfaceName;
        this.mInterfaceName = interfaceName;
        this.mWifiVendorHal = vendorHal;
        this.mSupplicantStaIfaceHal = staIfaceHal;
        this.mWificondControl = condControl;
        this.mWifiInjector = wifiInjector;
    }

    public String getInterfaceName() {
        return this.mInterfaceName;
    }

    public void enableVerboseLogging(int verbose) {
        boolean z;
        boolean z2 = true;
        WificondControl wificondControl = this.mWificondControl;
        if (verbose > 0) {
            z = true;
        } else {
            z = false;
        }
        wificondControl.enableVerboseLogging(z);
        SupplicantStaIfaceHal supplicantStaIfaceHal = this.mSupplicantStaIfaceHal;
        if (verbose > 0) {
            z = true;
        } else {
            z = false;
        }
        supplicantStaIfaceHal.enableVerboseLogging(z);
        WifiVendorHal wifiVendorHal = this.mWifiVendorHal;
        if (verbose <= 0) {
            z2 = false;
        }
        wifiVendorHal.enableVerboseLogging(z2);
    }

    public void setStaSoftApConcurrency(boolean enable) {
        this.mStaAndAPConcurrency = enable;
    }

    public Pair<Integer, IClientInterface> setupForClientMode() {
        if (startHalIfNecessary(true)) {
            IClientInterface iClientInterface = this.mWificondControl.setupDriverForClientMode();
            if (iClientInterface == null) {
                return Pair.create(Integer.valueOf(2), null);
            }
            return Pair.create(Integer.valueOf(0), iClientInterface);
        }
        Log.e(this.mTAG, "Failed to start HAL for client mode");
        return Pair.create(Integer.valueOf(1), null);
    }

    public Pair<Integer, IApInterface> setupForSoftApMode(String SapInterfaceName, boolean isDualMode) {
        if (startHalIfNecessary(false)) {
            if (SapInterfaceName != null) {
                addOrRemoveInterface(SapInterfaceName, true, isDualMode);
            }
            IApInterface iApInterface = this.mWificondControl.QcSetupDriverForSoftApMode(isDualMode);
            if (iApInterface == null) {
                return Pair.create(Integer.valueOf(2), null);
            }
            return Pair.create(Integer.valueOf(0), iApInterface);
        }
        Log.e(this.mTAG, "Failed to start HAL for AP mode");
        return Pair.create(Integer.valueOf(1), null);
    }

    public void tearDown() {
        stopHalIfNecessary();
        if (!this.mWificondControl.tearDownInterfaces()) {
            Log.e(this.mTAG, "Failed to teardown interfaces from Wificond");
        }
    }

    public boolean tearDownSta() {
        if (this.mWificondControl.tearDownStaInterfaces()) {
            return stopHalIfaceIfNecessary(true);
        }
        Log.e(this.mTAG, "Failed to teardown Sta interfaces from Wificond");
        return false;
    }

    public boolean tearDownAp() {
        if (this.mWificondControl.tearDownApInterfaces()) {
            return stopHalIfaceIfNecessary(false);
        }
        Log.e(this.mTAG, "Failed to teardown Ap interfaces from Wificond");
        return false;
    }

    private void stopHalIfNecessary() {
        if (this.mWifiVendorHal.isVendorHalSupported()) {
            this.mWifiVendorHal.stopVendorHal();
        } else {
            Log.i(this.mTAG, "Vendor HAL not supported, Ignore stop...");
        }
    }

    public boolean runQsapCmd(String cmd, String arg) {
        String strcmd = arg != null ? cmd + arg : cmd;
        if (this.mWificondControl.runQsapCmd(strcmd)) {
            return true;
        }
        Log.e(this.mTAG, "Failed to run QSAP command = " + strcmd);
        return false;
    }

    public boolean addOrRemoveInterface(String interfaceName, boolean add, boolean isBridge) {
        boolean status = false;
        if (interfaceName == null || (isBridge ^ 1) == 0) {
            if (interfaceName != null && isBridge) {
                if (add && runQsapCmd("softap bridge create ", interfaceName)) {
                    Log.d(this.mTAG, "created bridge SAP interface " + interfaceName);
                } else if (add || !runQsapCmd("softap bridge remove ", interfaceName)) {
                    Log.e(this.mTAG, "Failed to add/remove Bridge SAP interface " + interfaceName);
                    return false;
                } else {
                    Log.d(this.mTAG, "removed bridge SAP interface " + interfaceName);
                }
                String[] dualApInterfaces = this.mWifiInjector.getWifiApConfigStore().getDualSapInterfaces();
                if (dualApInterfaces == null || dualApInterfaces.length != 2) {
                    Log.e(this.mTAG, "dualApInterfaces is not set or length is not 2");
                    return false;
                } else if (add && runQsapCmd("softap create ", dualApInterfaces[0]) && runQsapCmd("softap create ", dualApInterfaces[1])) {
                    Log.d(this.mTAG, "created SAP interfaces " + dualApInterfaces[0] + " and " + dualApInterfaces[1]);
                    status = true;
                } else if (!add && runQsapCmd("softap remove ", dualApInterfaces[0]) && runQsapCmd("softap remove ", dualApInterfaces[1])) {
                    Log.d(this.mTAG, "removed SAP interfaces " + dualApInterfaces[0] + " and " + dualApInterfaces[1]);
                    status = true;
                } else {
                    Log.e(this.mTAG, "Failed to add/remove SAP interfaces " + dualApInterfaces[0] + " and " + dualApInterfaces[1]);
                }
            }
        } else if (add && runQsapCmd("softap create ", interfaceName)) {
            Log.d(this.mTAG, "created SAP interface " + interfaceName);
            status = true;
        } else if (add || !runQsapCmd("softap remove ", interfaceName)) {
            Log.e(this.mTAG, "Failed to add/remove SAP interface " + interfaceName);
        } else {
            Log.d(this.mTAG, "removed SAP interface " + interfaceName);
            status = true;
        }
        return status;
    }

    public boolean disableSupplicant() {
        return this.mWificondControl.disableSupplicant();
    }

    public boolean enableSupplicant() {
        return this.mWificondControl.enableSupplicant();
    }

    public SignalPollResult signalPoll() {
        return this.mWificondControl.signalPoll();
    }

    public TxPacketCounters getTxPacketCounters() {
        return this.mWificondControl.getTxPacketCounters();
    }

    public boolean scan(Set<Integer> freqs, Set<String> hiddenNetworkSSIDs) {
        return this.mWificondControl.scan(freqs, hiddenNetworkSSIDs);
    }

    public ArrayList<ScanDetail> getScanResults() {
        return this.mWificondControl.getScanResults(0);
    }

    public ArrayList<ScanDetail> getPnoScanResults() {
        return this.mWificondControl.getScanResults(1);
    }

    public boolean startPnoScan(PnoSettings pnoSettings) {
        return this.mWificondControl.startPnoScan(pnoSettings);
    }

    public boolean stopPnoScan() {
        return this.mWificondControl.stopPnoScan();
    }

    public boolean connectToSupplicant() {
        if (this.mSupplicantStaIfaceHal.isInitializationStarted() || (this.mSupplicantStaIfaceHal.initialize() ^ 1) == 0) {
            return this.mSupplicantStaIfaceHal.isInitializationComplete();
        }
        return false;
    }

    public void closeSupplicantConnection() {
    }

    public void setSupplicantLogLevel(boolean turnOnVerbose) {
        this.mSupplicantStaIfaceHal.setLogLevel(turnOnVerbose);
    }

    public boolean reconnect() {
        return this.mSupplicantStaIfaceHal.reconnect();
    }

    public boolean manuReassociate() {
        return this.mSupplicantStaIfaceHal.manuReassociate();
    }

    public void updateCurrentConfigPairByWps(int frameworkNetId) {
        this.mSupplicantStaIfaceHal.updateCurrentConfigPairByWps(frameworkNetId);
    }

    public boolean reassociate() {
        return this.mSupplicantStaIfaceHal.reassociate();
    }

    public boolean disconnect() {
        return this.mSupplicantStaIfaceHal.disconnect();
    }

    public String getMacAddress() {
        return this.mSupplicantStaIfaceHal.getMacAddress();
    }

    public boolean startFilteringMulticastV4Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.removeRxFilter(0)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean stopFilteringMulticastV4Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.addRxFilter(0)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean startFilteringMulticastV6Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.removeRxFilter(1)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean stopFilteringMulticastV6Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.addRxFilter(1)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean setBluetoothCoexistenceMode(int mode) {
        return this.mSupplicantStaIfaceHal.setBtCoexistenceMode(mode);
    }

    public boolean setBluetoothCoexistenceScanMode(boolean setCoexScanMode) {
        return this.mSupplicantStaIfaceHal.setBtCoexistenceScanModeEnabled(setCoexScanMode);
    }

    public boolean setSuspendOptimizations(boolean enabled) {
        return this.mSupplicantStaIfaceHal.setSuspendModeEnabled(enabled);
    }

    public boolean setCountryCode(String countryCode) {
        return this.mSupplicantStaIfaceHal.setCountryCode(countryCode);
    }

    public boolean flushAllHlp() {
        return this.mSupplicantStaIfaceHal.flushAllHlp();
    }

    public boolean addHlpReq(String dst, String hlpPacket) {
        return this.mSupplicantStaIfaceHal.addHlpReq(dst, hlpPacket);
    }

    public void startTdls(String macAddr, boolean enable) {
        if (enable) {
            this.mSupplicantStaIfaceHal.initiateTdlsDiscover(macAddr);
            this.mSupplicantStaIfaceHal.initiateTdlsSetup(macAddr);
            return;
        }
        this.mSupplicantStaIfaceHal.initiateTdlsTeardown(macAddr);
    }

    public boolean startWpsPbc(String bssid) {
        return this.mSupplicantStaIfaceHal.startWpsPbc(bssid);
    }

    public boolean startWpsPinKeypad(String pin) {
        return this.mSupplicantStaIfaceHal.startWpsPinKeypad(pin);
    }

    public String startWpsPinDisplay(String bssid) {
        return this.mSupplicantStaIfaceHal.startWpsPinDisplay(bssid);
    }

    public boolean setExternalSim(boolean external) {
        return this.mSupplicantStaIfaceHal.setExternalSim(external);
    }

    public boolean simAuthResponse(int id, String type, String response) {
        if (SIM_AUTH_RESP_TYPE_GSM_AUTH.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimGsmAuthResponse(response);
        }
        if (SIM_AUTH_RESP_TYPE_UMTS_AUTH.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAuthResponse(response);
        }
        if (SIM_AUTH_RESP_TYPE_UMTS_AUTS.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAutsResponse(response);
        }
        return false;
    }

    public boolean simAuthFailedResponse(int id) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimGsmAuthFailure();
    }

    public boolean umtsAuthFailedResponse(int id) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAuthFailure();
    }

    public boolean simIdentityResponse(int id, String response) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapIdentityResponse(response);
    }

    public String getEapAnonymousIdentity() {
        return this.mSupplicantStaIfaceHal.getCurrentNetworkEapAnonymousIdentity();
    }

    public boolean startWpsRegistrar(String bssid, String pin) {
        return this.mSupplicantStaIfaceHal.startWpsRegistrar(bssid, pin);
    }

    public boolean cancelWps() {
        return this.mSupplicantStaIfaceHal.cancelWps();
    }

    public boolean setDeviceName(String name) {
        return this.mSupplicantStaIfaceHal.setWpsDeviceName(name);
    }

    public boolean setDeviceType(String type) {
        return this.mSupplicantStaIfaceHal.setWpsDeviceType(type);
    }

    public boolean setConfigMethods(String cfg) {
        return this.mSupplicantStaIfaceHal.setWpsConfigMethods(cfg);
    }

    public boolean setManufacturer(String value) {
        return this.mSupplicantStaIfaceHal.setWpsManufacturer(value);
    }

    public boolean setModelName(String value) {
        return this.mSupplicantStaIfaceHal.setWpsModelName(value);
    }

    public boolean setModelNumber(String value) {
        return this.mSupplicantStaIfaceHal.setWpsModelNumber(value);
    }

    public boolean setSerialNumber(String value) {
        return this.mSupplicantStaIfaceHal.setWpsSerialNumber(value);
    }

    public void setPowerSave(boolean enabled) {
        this.mSupplicantStaIfaceHal.setPowerSave(enabled);
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        return this.mSupplicantStaIfaceHal.setConcurrencyPriority(isStaHigherPriority);
    }

    public boolean enableStaAutoReconnect(boolean enable) {
        return this.mSupplicantStaIfaceHal.enableAutoReconnect(enable);
    }

    public boolean migrateNetworksFromSupplicant(Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        if (this.mSupplicantStaIfaceHal.loadNetworks(configs, networkExtras)) {
            for (Entry<String, WifiConfiguration> entry : configs.entrySet()) {
                WifiConfiguration config = (WifiConfiguration) entry.getValue();
                ArrayList<Byte> ssid = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(config.SSID));
                ArrayList<Byte> out_ssid = this.mWificondControl.getWifiGbkHistory(ssid);
                Log.d(this.mTAG, "ssid arraylist = " + ssid);
                Log.d(this.mTAG, "out_ssid arraylist = " + out_ssid);
                if (!(ssid == null || out_ssid == null)) {
                    config.SSID = NativeUtil.encodeSsid(out_ssid);
                }
                Log.d(this.mTAG, "after convert, ssid = " + config.SSID + ", bssid = " + config.BSSID);
            }
            return true;
        }
        Log.e(this.mTAG, "Failed to load networks!");
        return false;
    }

    public String ssidStrFromGbkHistory(String ssid_str) {
        if (ssid_str == null || ssid_str.length() == 0) {
            return ssid_str;
        }
        try {
            ArrayList<Byte> ssid = NativeUtil.decodeSsid(ssid_str);
            ArrayList<Byte> out_ssid = this.mWificondControl.getWifiGbkHistory(ssid);
            if (ssid == null || (ssid.equals(out_ssid) ^ 1) == 0) {
                return ssid_str;
            }
            return NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(out_ssid));
        } catch (IllegalArgumentException e) {
            Log.i(this.mTAG, "Illegal argument " + ssid_str);
            return ssid_str;
        }
    }

    public WifiSsid wifiSsidFromGbkHistory(WifiSsid ssid_st) {
        if (ssid_st == null || ssid_st.isHidden()) {
            return ssid_st;
        }
        ArrayList<Byte> ssid = NativeUtil.byteArrayToArrayList(ssid_st.getOctets());
        ArrayList<Byte> out_ssid = this.mWificondControl.getWifiGbkHistory(ssid);
        if (ssid == null || (ssid.equals(out_ssid) ^ 1) == 0) {
            return ssid_st;
        }
        return WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(out_ssid));
    }

    public boolean connectToNetwork(WifiConfiguration configuration) {
        this.mWificondControl.abortScan();
        configuration.SSID = ssidStrFromGbkHistory(configuration.SSID);
        return this.mSupplicantStaIfaceHal.connectToNetwork(configuration);
    }

    public boolean roamToNetwork(WifiConfiguration configuration) {
        this.mWificondControl.abortScan();
        configuration.SSID = ssidStrFromGbkHistory(configuration.SSID);
        return this.mSupplicantStaIfaceHal.roamToNetwork(configuration);
    }

    public int getFrameworkNetworkId(int supplicantNetworkId) {
        return supplicantNetworkId;
    }

    public boolean removeAllNetworks() {
        return this.mSupplicantStaIfaceHal.removeAllNetworks();
    }

    public boolean setConfiguredNetworkBSSID(String bssid) {
        return this.mSupplicantStaIfaceHal.setCurrentNetworkBssid(bssid);
    }

    public boolean requestAnqp(String bssid, Set<Integer> anqpIds, Set<Integer> hs20Subtypes) {
        if (bssid == null || ((anqpIds == null || anqpIds.isEmpty()) && (hs20Subtypes == null || hs20Subtypes.isEmpty()))) {
            Log.e(this.mTAG, "Invalid arguments for ANQP request.");
            return false;
        }
        ArrayList anqpIdList = new ArrayList();
        for (Integer anqpId : anqpIds) {
            anqpIdList.add(Short.valueOf(anqpId.shortValue()));
        }
        ArrayList hs20SubtypeList = new ArrayList();
        hs20SubtypeList.addAll(hs20Subtypes);
        return this.mSupplicantStaIfaceHal.initiateAnqpQuery(bssid, anqpIdList, hs20SubtypeList);
    }

    public boolean requestIcon(String bssid, String fileName) {
        if (bssid != null && fileName != null) {
            return this.mSupplicantStaIfaceHal.initiateHs20IconQuery(bssid, fileName);
        }
        Log.e(this.mTAG, "Invalid arguments for Icon request.");
        return false;
    }

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        return this.mSupplicantStaIfaceHal.getCurrentNetworkWpsNfcConfigurationToken();
    }

    public void removeNetworkIfCurrent(int networkId) {
        this.mSupplicantStaIfaceHal.removeNetworkIfCurrent(networkId);
    }

    public boolean initializeVendorHal(VendorHalDeathEventHandler handler) {
        return this.mWifiVendorHal.initialize(handler);
    }

    private boolean startHalIfNecessary(boolean isStaMode) {
        if (!this.mWifiVendorHal.isVendorHalSupported()) {
            Log.i(this.mTAG, "Vendor HAL not supported, Ignore start...");
            return true;
        } else if (this.mStaAndAPConcurrency) {
            return this.mWifiVendorHal.startConcurrentVendorHal(isStaMode);
        } else {
            return this.mWifiVendorHal.startVendorHal(isStaMode);
        }
    }

    private boolean stopHalIfaceIfNecessary(boolean isSta) {
        if (this.mWifiVendorHal.isVendorHalSupported()) {
            return this.mWifiVendorHal.stopVendorHalIface(isSta);
        }
        Log.i(this.mTAG, "Vendor Iface HAL not supported, Ignore stop...");
        return false;
    }

    public boolean isHalStarted() {
        return this.mWifiVendorHal.isHalStarted();
    }

    public boolean getBgScanCapabilities(ScanCapabilities capabilities) {
        return this.mWifiVendorHal.getBgScanCapabilities(capabilities);
    }

    public boolean startBgScan(ScanSettings settings, ScanEventHandler eventHandler) {
        return this.mWifiVendorHal.startBgScan(settings, eventHandler);
    }

    public void stopBgScan() {
        this.mWifiVendorHal.stopBgScan();
    }

    public void pauseBgScan() {
        this.mWifiVendorHal.pauseBgScan();
    }

    public void restartBgScan() {
        this.mWifiVendorHal.restartBgScan();
    }

    public ScanData[] getBgScanResults() {
        return this.mWifiVendorHal.getBgScanResults();
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String iface) {
        return this.mWifiVendorHal.getWifiLinkLayerStats();
    }

    public int getSupportedFeatureSet() {
        return this.mWifiVendorHal.getSupportedFeatureSet();
    }

    public boolean requestRtt(RttParams[] params, RttEventHandler handler) {
        return this.mWifiVendorHal.requestRtt(params, handler);
    }

    public boolean cancelRtt(RttParams[] params) {
        return this.mWifiVendorHal.cancelRtt(params);
    }

    public ResponderConfig enableRttResponder(int timeoutSeconds) {
        return this.mWifiVendorHal.enableRttResponder(timeoutSeconds);
    }

    public boolean disableRttResponder() {
        return this.mWifiVendorHal.disableRttResponder();
    }

    public boolean setScanningMacOui(byte[] oui) {
        return this.mWifiVendorHal.setScanningMacOui(oui);
    }

    public int[] getChannelsForBand(int band) {
        return this.mWifiVendorHal.getChannelsForBand(band);
    }

    public boolean isGetChannelsForBandSupported() {
        return this.mWifiVendorHal.isGetChannelsForBandSupported();
    }

    public RttCapabilities getRttCapabilities() {
        return this.mWifiVendorHal.getRttCapabilities();
    }

    public ApfCapabilities getApfCapabilities() {
        return this.mWifiVendorHal.getApfCapabilities();
    }

    public boolean installPacketFilter(byte[] filter) {
        return this.mWifiVendorHal.installPacketFilter(filter);
    }

    public boolean setCountryCodeHal(String countryCode) {
        return this.mWifiVendorHal.setCountryCodeHal(countryCode);
    }

    public boolean setLoggingEventHandler(WifiLoggerEventHandler handler) {
        return this.mWifiVendorHal.setLoggingEventHandler(handler);
    }

    public boolean startLoggingRingBuffer(int verboseLevel, int flags, int maxInterval, int minDataSize, String ringName) {
        return this.mWifiVendorHal.startLoggingRingBuffer(verboseLevel, flags, maxInterval, minDataSize, ringName);
    }

    public int getSupportedLoggerFeatureSet() {
        return this.mWifiVendorHal.getSupportedLoggerFeatureSet();
    }

    public boolean resetLogHandler() {
        return this.mWifiVendorHal.resetLogHandler();
    }

    public String getDriverVersion() {
        return this.mWifiVendorHal.getDriverVersion();
    }

    public String getFirmwareVersion() {
        return this.mWifiVendorHal.getFirmwareVersion();
    }

    public RingBufferStatus[] getRingBufferStatus() {
        return this.mWifiVendorHal.getRingBufferStatus();
    }

    public boolean getRingBufferData(String ringName) {
        return this.mWifiVendorHal.getRingBufferData(ringName);
    }

    public byte[] getFwMemoryDump() {
        return this.mWifiVendorHal.getFwMemoryDump();
    }

    public byte[] getDriverStateDump() {
        return this.mWifiVendorHal.getDriverStateDump();
    }

    public boolean startPktFateMonitoring() {
        return this.mWifiVendorHal.startPktFateMonitoring();
    }

    public boolean getTxPktFates(TxFateReport[] reportBufs) {
        return this.mWifiVendorHal.getTxPktFates(reportBufs);
    }

    public boolean getRxPktFates(RxFateReport[] reportBufs) {
        return this.mWifiVendorHal.getRxPktFates(reportBufs);
    }

    public int startSendingOffloadedPacket(int slot, KeepalivePacketData keepAlivePacket, int period) {
        String[] macAddrStr = getMacAddress().split(":");
        byte[] srcMac = new byte[6];
        for (int i = 0; i < 6; i++) {
            srcMac[i] = Integer.valueOf(Integer.parseInt(macAddrStr[i], 16)).byteValue();
        }
        return this.mWifiVendorHal.startSendingOffloadedPacket(slot, srcMac, keepAlivePacket, period);
    }

    public int stopSendingOffloadedPacket(int slot) {
        return this.mWifiVendorHal.stopSendingOffloadedPacket(slot);
    }

    public int startRssiMonitoring(byte maxRssi, byte minRssi, WifiRssiEventHandler rssiEventHandler) {
        return this.mWifiVendorHal.startRssiMonitoring(maxRssi, minRssi, rssiEventHandler);
    }

    public int stopRssiMonitoring() {
        return this.mWifiVendorHal.stopRssiMonitoring();
    }

    public WifiWakeReasonAndCounts getWlanWakeReasonCount() {
        return this.mWifiVendorHal.getWlanWakeReasonCount();
    }

    public boolean configureNeighborDiscoveryOffload(boolean enabled) {
        return this.mWifiVendorHal.configureNeighborDiscoveryOffload(enabled);
    }

    public boolean getRoamingCapabilities(RoamingCapabilities capabilities) {
        return this.mWifiVendorHal.getRoamingCapabilities(capabilities);
    }

    public int enableFirmwareRoaming(int state) {
        return this.mWifiVendorHal.enableFirmwareRoaming(state);
    }

    public boolean configureRoaming(RoamingConfig config) {
        Log.d(this.mTAG, "configureRoaming ");
        return this.mWifiVendorHal.configureRoaming(config);
    }

    public boolean resetRoamingConfiguration() {
        return this.mWifiVendorHal.configureRoaming(new RoamingConfig());
    }

    public boolean selectTxPowerScenario(int scenario) {
        return this.mWifiVendorHal.selectTxPowerScenario(scenario);
    }

    static {
        System.loadLibrary("wifi-service");
        registerNatives();
    }

    public synchronized String readKernelLog() {
        byte[] bytes = readKernelLogNative();
        if (bytes != null) {
            try {
                return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException e) {
                return new String(bytes, StandardCharsets.ISO_8859_1);
            }
        }
        return "*** failed to read kernel log ***";
    }
}

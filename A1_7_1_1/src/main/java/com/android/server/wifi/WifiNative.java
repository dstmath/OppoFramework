package com.android.server.wifi;

import android.content.Context;
import android.net.apf.ApfCapabilities;
import android.net.wifi.RttManager.ResponderConfig;
import android.net.wifi.RttManager.RttCapabilities;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.RttManager.RttResult;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner.HotlistSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.WifiChangeSettings;
import android.net.wifi.WifiSsid;
import android.net.wifi.WifiWakeReasonAndCounts;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.Immutable;
import com.android.internal.util.HexDump;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.SupplicantBridge;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.MOTree;
import com.android.server.wifi.util.FrameParser;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.InformationElementUtil.Capabilities;
import com.android.server.wifi.util.InformationElementUtil.ExtendedCapabilities;
import com.android.server.wifi.util.InformationElementUtil.HtOperation;
import com.android.server.wifi.util.InformationElementUtil.SupportedRates;
import com.android.server.wifi.util.InformationElementUtil.VhtOperation;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.util.HexEncoding;
import org.json.JSONException;
import org.json.JSONObject;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiNative {
    public static final int BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    public static final int BLUETOOTH_COEXISTENCE_MODE_ENABLED = 0;
    public static final int BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    private static final String BSS_BSSID_STR = "bssid=";
    private static final String BSS_DELIMITER_STR = "====";
    private static final String BSS_END_STR = "####";
    private static final String BSS_FLAGS_STR = "flags=";
    private static final String BSS_FREQ_STR = "freq=";
    private static final String BSS_ID_STR = "id=";
    private static final String BSS_IE_STR = "ie=";
    private static final String BSS_LEVEL_STR = "level=";
    private static final String BSS_SSID_STR = "ssid=";
    private static final String BSS_TSF_STR = "tsf=";
    private static boolean DBG = false;
    private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
    private static int OPPO_POOR_RSSI = 0;
    private static final int STOP_HAL_TIMEOUT_MS = 1000;
    private static final String TAG = "WifiNative-HAL";
    public static final int WIFI_SCAN_FAILED = 3;
    public static final int WIFI_SCAN_RESULTS_AVAILABLE = 0;
    public static final int WIFI_SCAN_THRESHOLD_NUM_SCANS = 1;
    public static final int WIFI_SCAN_THRESHOLD_PERCENT = 2;
    public static final int WIFI_SUCCESS = 0;
    private static byte[] mFwMemoryDump;
    private static WifiNative p2pNativeInterface;
    private static int sCmdId;
    private static int sHotlistCmdId;
    private static HotlistEventHandler sHotlistEventHandler;
    private static AtomicBoolean sIsTerminate;
    private static final LocalLog sLocalLog = null;
    public static final Object sLock = null;
    private static int sLogCmdId;
    private static int sPnoCmdId;
    private static PnoEventHandler sPnoEventHandler;
    private static int sRssiMonitorCmdId;
    private static int sRttCmdId;
    private static RttEventHandler sRttEventHandler;
    private static int sRttResponderCmdId;
    private static int sScanCmdId;
    private static ScanEventHandler sScanEventHandler;
    private static ScanSettings sScanSettings;
    private static int sSignificantWifiChangeCmdId;
    private static SignificantWifiChangeEventHandler sSignificantWifiChangeHandler;
    private static TdlsEventHandler sTdlsEventHandler;
    private static MonitorThread sThread;
    private static long sWifiHalHandle;
    private static long[] sWifiIfaceHandles;
    private static WifiLoggerEventHandler sWifiLoggerEventHandler;
    private static WifiRssiEventHandler sWifiRssiEventHandler;
    public static int sWlan0Index;
    private static WifiNative wlanNativeInterface;
    private Context mContext;
    private boolean mDisconnectCalled;
    private final String mInterfaceName;
    private final String mInterfacePrefix;
    private final String mTAG;

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
        static final SimpleDateFormat dateFormatter = null;
        final long mDriverTimestampUSec;
        final long mEstimatedWallclockMSec;
        final byte mFate;
        final byte[] mFrameBytes;
        final byte mFrameType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiNative.FateReport.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiNative.FateReport.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiNative.FateReport.<clinit>():void");
        }

        protected abstract String directionToString();

        protected abstract String fateToString();

        FateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            this.mFate = fate;
            this.mDriverTimestampUSec = driverTimestampUSec;
            this.mEstimatedWallclockMSec = convertDriverTimestampUSecToWallclockMSec(this.mDriverTimestampUSec);
            this.mFrameType = frameType;
            this.mFrameBytes = frameBytes;
        }

        public String toTableRowString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            Object[] objArr = new Object[7];
            objArr[0] = Long.valueOf(this.mDriverTimestampUSec);
            objArr[1] = dateFormatter.format(new Date(this.mEstimatedWallclockMSec));
            objArr[2] = directionToString();
            objArr[3] = fateToString();
            objArr[4] = parser.mMostSpecificProtocolString;
            objArr[5] = parser.mTypeString;
            objArr[6] = parser.mResultString;
            pw.format("%-15s  %12s  %-9s  %-32s  %-12s  %-23s  %s\n", objArr);
            return sw.toString();
        }

        public String toVerboseStringWithPiiAllowed() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            Object[] objArr = new Object[1];
            objArr[0] = directionToString();
            pw.format("Frame direction: %s\n", objArr);
            objArr = new Object[1];
            objArr[0] = Long.valueOf(this.mDriverTimestampUSec);
            pw.format("Frame timestamp: %d\n", objArr);
            objArr = new Object[1];
            objArr[0] = fateToString();
            pw.format("Frame fate: %s\n", objArr);
            objArr = new Object[1];
            objArr[0] = frameTypeToString(this.mFrameType);
            pw.format("Frame type: %s\n", objArr);
            objArr = new Object[1];
            objArr[0] = parser.mMostSpecificProtocolString;
            pw.format("Frame protocol: %s\n", objArr);
            objArr = new Object[1];
            objArr[0] = parser.mTypeString;
            pw.format("Frame protocol type: %s\n", objArr);
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(this.mFrameBytes.length);
            pw.format("Frame length: %d\n", objArr);
            pw.append("Frame bytes");
            pw.append(HexDump.dumpHexString(this.mFrameBytes));
            pw.append("\n");
            return sw.toString();
        }

        public static String getTableHeader() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            Object[] objArr = new Object[7];
            objArr[0] = "Time usec";
            objArr[1] = "Walltime";
            objArr[2] = "Direction";
            objArr[3] = "Fate";
            objArr[4] = "Protocol";
            objArr[5] = MOTree.TypeTag;
            objArr[6] = "Result";
            pw.format("\n%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", objArr);
            objArr = new Object[7];
            objArr[0] = "---------";
            objArr[1] = "--------";
            objArr[2] = "---------";
            objArr[3] = "----";
            objArr[4] = "--------";
            objArr[5] = "----";
            objArr[6] = "------";
            pw.format("%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", objArr);
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

    public interface HotlistEventHandler {
        void onHotlistApFound(ScanResult[] scanResultArr);

        void onHotlistApLost(ScanResult[] scanResultArr);
    }

    private static class MonitorThread extends Thread {
        /* synthetic */ MonitorThread(MonitorThread monitorThread) {
            this();
        }

        private MonitorThread() {
        }

        public void run() {
            Log.i(WifiNative.TAG, "Waiting for HAL events mWifiHalHandle=" + Long.toString(WifiNative.sWifiHalHandle));
            WifiNative.waitForHalEventNative();
        }
    }

    public interface PnoEventHandler {
        void onPnoNetworkFound(ScanResult[] scanResultArr);

        void onPnoScanFailed();
    }

    public static class PnoNetwork {
        public byte auth_bit_field;
        public byte flags;
        public int networkId;
        public int priority;
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
            if (!Objects.equals(this.ssid, other.ssid) || this.networkId != other.networkId || this.priority != other.priority || this.flags != other.flags) {
                z = false;
            } else if (this.auth_bit_field != other.auth_bit_field) {
                z = false;
            }
            return z;
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
        public int sameNetworkBonus;
        public int secureBonus;
    }

    public static class RingBufferStatus {
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
        public int max_bssid_history_entries;
        public int max_hotlist_bssids;
        public int max_number_epno_networks;
        public int max_number_epno_networks_by_ssid;
        public int max_number_of_white_listed_ssid;
        public int max_rssi_sample_size;
        public int max_scan_buckets;
        public int max_scan_cache_size;
        public int max_scan_reporting_threshold;
        public int max_significant_wifi_change_aps;
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
        public int[] hiddenNetworkIds;
        public int max_ap_per_scan;
        public int num_buckets;
        public int report_threshold_num_scans;
        public int report_threshold_percent;
    }

    public interface SignificantWifiChangeEventHandler {
        void onChangesFound(ScanResult[] scanResultArr);
    }

    public static class TdlsCapabilities {
        boolean isGlobalTdlsSupported;
        boolean isOffChannelTdlsSupported;
        boolean isPerMacTdlsSupported;
        int maxConcurrentTdlsSessionNumber;
    }

    public abstract class TdlsEventHandler {
        public abstract void onTdlsStatus(String str, int i, int i2);
    }

    public static class TdlsStatus {
        int channel;
        int global_operating_class;
        int reason;
        int state;
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

    public static class WifiChannelInfo {
        int mCenterFrequency0;
        int mCenterFrequency1;
        int mChannelWidth;
        int mPrimaryFrequency;
    }

    public interface WifiRssiEventHandler {
        void onRssiThresholdBreached(byte b);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiNative.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiNative.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiNative.<clinit>():void");
    }

    private static native boolean cancelRangeRequestNative(int i, int i2, RttParams[] rttParamsArr);

    private static native void closeSupplicantConnectionNative();

    private static native int configureNeighborDiscoveryOffload(int i, boolean z);

    private static native boolean connectToSupplicantNative();

    private static native boolean disableRttResponderNative(int i, int i2);

    private native boolean doBooleanCommandNative(String str);

    private native int doIntCommandNative(String str);

    private native String doStringCommandNative(String str);

    private static native boolean enableDisableTdlsNative(int i, boolean z, String str);

    private static native ResponderConfig enableRttResponderNative(int i, int i2, int i3, WifiChannelInfo wifiChannelInfo);

    private static native ApfCapabilities getApfCapabilitiesNative(int i);

    private static native int[] getChannelsForBandNative(int i, int i2);

    private static native byte[] getDriverStateDumpNative(int i);

    private static native String getDriverVersionNative(int i);

    private static native String getFirmwareVersionNative(int i);

    private static native boolean getFwMemoryDumpNative(int i);

    private static native String getInterfaceNameNative(int i);

    private static native int getInterfacesNative();

    private static native boolean getRingBufferDataNative(int i, String str);

    private static native RingBufferStatus[] getRingBufferStatusNative(int i);

    private static native RttCapabilities getRttCapabilitiesNative(int i);

    private static native int getRxPktFatesNative(int i, RxFateReport[] rxFateReportArr);

    private static native boolean getScanCapabilitiesNative(int i, ScanCapabilities scanCapabilities);

    private static native ScanData[] getScanResultsNative(int i, boolean z);

    public static native int getSupportedFeatureSetNative(int i);

    private static native int getSupportedLoggerFeatureSetNative(int i);

    private static native TdlsCapabilities getTdlsCapabilitiesNative(int i);

    private static native TdlsStatus getTdlsStatusNative(int i, String str);

    private static native int getTxPktFatesNative(int i, TxFateReport[] txFateReportArr);

    private static native WifiLinkLayerStats getWifiLinkLayerStatsNative(int i);

    private static native WifiWakeReasonAndCounts getWlanWakeReasonCountNative(int i);

    private static native boolean installPacketFilterNative(int i, byte[] bArr);

    private static native boolean isDriverLoadedNative();

    private static native boolean isGetChannelsForBandSupportedNative();

    private static native boolean killSupplicantNative(boolean z);

    private static native boolean loadDriverNative();

    private static native byte[] readKernelLogNative();

    private static native int registerNatives();

    private static native boolean requestRangeNative(int i, int i2, RttParams[] rttParamsArr);

    private static native boolean resetHotlistNative(int i, int i2);

    private static native boolean resetLogHandlerNative(int i, int i2);

    private static native boolean resetPnoListNative(int i, int i2);

    private static native boolean setBssidBlacklistNative(int i, int i2, String[] strArr);

    private static native boolean setCountryCodeHalNative(int i, String str);

    private static native boolean setDfsFlagNative(int i, boolean z);

    private static native boolean setHotlistNative(int i, int i2, HotlistSettings hotlistSettings);

    private static native boolean setInterfaceUpNative(boolean z);

    private static native boolean setLoggingEventHandlerNative(int i, int i2);

    private native boolean setNetworkVariableCommand(String str, int i, String str2, String str3);

    private static native boolean setPnoListNative(int i, int i2, PnoSettings pnoSettings);

    private static native boolean setScanningMacOuiNative(int i, byte[] bArr);

    public static native boolean setTxPower(int i);

    public static native boolean setTxPowerEnabled(boolean z);

    private static native void setWifiLinkLayerStatsNative(int i, int i2);

    private static native boolean startHalNative();

    private static native boolean startLoggingRingBufferNative(int i, int i2, int i3, int i4, int i5, String str);

    private static native int startPktFateMonitoringNative(int i);

    private static native int startRssiMonitoringNative(int i, int i2, byte b, byte b2);

    private static native boolean startScanNative(int i, int i2, ScanSettings scanSettings);

    private static native int startSendingOffloadedPacketNative(int i, int i2, byte[] bArr, byte[] bArr2, byte[] bArr3, int i3);

    private static native boolean startSupplicantNative(boolean z);

    private static native void stopHalNative();

    private static native int stopRssiMonitoringNative(int i, int i2);

    private static native boolean stopScanNative(int i, int i2);

    private static native int stopSendingOffloadedPacketNative(int i, int i2);

    private static native boolean trackSignificantWifiChangeNative(int i, int i2, WifiChangeSettings wifiChangeSettings);

    private static native boolean unloadDriverNative();

    private static native boolean untrackSignificantWifiChangeNative(int i, int i2);

    private static native String waitForEventNative();

    private static native void waitForHalEventNative();

    public LocalLog getLocalLog() {
        return sLocalLog;
    }

    public static WifiNative getWlanNativeInterface() {
        return wlanNativeInterface;
    }

    public static WifiNative getP2pNativeInterface() {
        return p2pNativeInterface;
    }

    public void initContext(Context context) {
        if (this.mContext == null && context != null) {
            this.mContext = context;
        }
    }

    private WifiNative(String interfaceName, boolean requiresPrefix) {
        this.mContext = null;
        this.mDisconnectCalled = false;
        this.mInterfaceName = interfaceName;
        this.mTAG = "WifiNative-" + interfaceName;
        if (requiresPrefix) {
            this.mInterfacePrefix = "IFNAME=" + interfaceName + " ";
        } else {
            this.mInterfacePrefix = "";
        }
    }

    public String getInterfaceName() {
        return this.mInterfaceName;
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    private void localLog(String s) {
        if (sLocalLog != null) {
            sLocalLog.log(this.mInterfaceName + ": " + s);
        }
    }

    public boolean loadDriver() {
        boolean loadDriverNative;
        synchronized (sLock) {
            loadDriverNative = loadDriverNative();
        }
        return loadDriverNative;
    }

    public boolean isDriverLoaded() {
        boolean isDriverLoadedNative;
        synchronized (sLock) {
            isDriverLoadedNative = isDriverLoadedNative();
        }
        return isDriverLoadedNative;
    }

    public boolean unloadDriver() {
        boolean unloadDriverNative;
        synchronized (sLock) {
            unloadDriverNative = unloadDriverNative();
        }
        return unloadDriverNative;
    }

    public boolean startSupplicant(boolean p2pSupported) {
        boolean startSupplicantNative;
        synchronized (sLock) {
            Log.d(this.mTAG, "sIsTerminate set false");
            sIsTerminate.set(false);
            startSupplicantNative = startSupplicantNative(p2pSupported);
        }
        return startSupplicantNative;
    }

    public boolean killSupplicant(boolean p2pSupported) {
        boolean killSupplicantNative;
        synchronized (sLock) {
            killSupplicantNative = killSupplicantNative(p2pSupported);
        }
        return killSupplicantNative;
    }

    public boolean connectToSupplicant() {
        boolean connectToSupplicantNative;
        synchronized (sLock) {
            localLog(this.mInterfacePrefix + "connectToSupplicant");
            connectToSupplicantNative = connectToSupplicantNative();
        }
        return connectToSupplicantNative;
    }

    public void closeSupplicantConnection() {
        synchronized (sLock) {
            localLog(this.mInterfacePrefix + "closeSupplicantConnection");
            closeSupplicantConnectionNative();
        }
    }

    public String waitForEvent() {
        return waitForEventNative();
    }

    /* JADX WARNING: Missing block: B:17:0x00b1, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doBooleanCommand(String command) {
        if (DBG) {
            Log.d(this.mTAG, "doBoolean: " + command);
        }
        synchronized (sLock) {
            if (!sIsTerminate.get() || this.mInterfacePrefix.contains("ap0")) {
                if (command.equals("TERMINATE")) {
                    Log.d(this.mTAG, "sIsTerminate set true");
                    sIsTerminate.set(true);
                }
                String toLog = this.mInterfacePrefix + command;
                boolean result = doBooleanCommandNative(this.mInterfacePrefix + command);
                localLog(toLog + " -> " + result);
                if (DBG) {
                    Log.d(this.mTAG, command + ": returned " + result);
                }
            } else {
                Log.d(this.mTAG, "Wifi is terminating. Don't do native commands, directly return null.");
                return false;
            }
        }
    }

    private boolean doBooleanCommandWithoutLogging(String command) {
        boolean result;
        if (DBG) {
            Log.d(this.mTAG, "doBooleanCommandWithoutLogging: " + command);
        }
        synchronized (sLock) {
            result = doBooleanCommandNative(this.mInterfacePrefix + command);
            if (DBG) {
                Log.d(this.mTAG, command + ": returned " + result);
            }
        }
        return result;
    }

    private int doIntCommand(String command) {
        int result;
        if (DBG) {
            Log.d(this.mTAG, "doInt: " + command);
        }
        synchronized (sLock) {
            String toLog = this.mInterfacePrefix + command;
            result = doIntCommandNative(this.mInterfacePrefix + command);
            localLog(toLog + " -> " + result);
            if (DBG) {
                Log.d(this.mTAG, "   returned " + result);
            }
        }
        return result;
    }

    private String doStringCommand(String command) {
        String result;
        if (DBG && !command.startsWith("GET_NETWORK")) {
            Log.d(this.mTAG, "doString: [" + command + "]");
        }
        synchronized (sLock) {
            String toLog = this.mInterfacePrefix + command;
            result = doStringCommandNative(this.mInterfacePrefix + command);
            if (result != null) {
                if (!command.startsWith("STATUS-")) {
                    localLog(toLog + " -> " + result);
                }
                if (DBG) {
                    Log.d(this.mTAG, "   returned " + result.replace("\n", " "));
                }
            } else if (DBG) {
                Log.d(this.mTAG, "doStringCommandNative no result");
            }
        }
        return result;
    }

    private String doStringCommandWithoutLogging(String command) {
        if (DBG && !command.startsWith("GET_NETWORK")) {
            Log.d(this.mTAG, "doString: [" + command + "]");
        }
        synchronized (sLock) {
            if (sIsTerminate.get()) {
                Log.d(this.mTAG, "Wifi is terminating. Don't do native commands, directly return null.");
                return null;
            }
            String doStringCommandNative = doStringCommandNative(this.mInterfacePrefix + command);
            return doStringCommandNative;
        }
    }

    public String doCustomSupplicantCommand(String command) {
        return doStringCommand(command);
    }

    public boolean ping() {
        String pong = doStringCommand("PING");
        return pong != null ? pong.equals("PONG") : false;
    }

    public void setSupplicantLogLevel(String level) {
        doStringCommand("LOG_LEVEL " + level);
    }

    public String getFreqCapability() {
        return doStringCommand("GET_CAPABILITY freq");
    }

    public String setOshareMod(String mod) {
        return doStringCommand("DRIVER OSHAREMOD " + mod);
    }

    private static String createCSVStringFromIntegerSet(Set<Integer> values) {
        StringBuilder list = new StringBuilder();
        boolean first = true;
        for (Integer value : values) {
            if (!first) {
                list.append(",");
            }
            list.append(value);
            first = false;
        }
        return list.toString();
    }

    public boolean scan(Set<Integer> freqs, Set<Integer> hiddenNetworkIds) {
        String freqList = null;
        String hiddenNetworkIdList = null;
        if (!(freqs == null || freqs.size() == 0)) {
            freqList = createCSVStringFromIntegerSet(freqs);
        }
        if (!(hiddenNetworkIds == null || hiddenNetworkIds.size() == 0)) {
            hiddenNetworkIdList = createCSVStringFromIntegerSet(hiddenNetworkIds);
        }
        return scanWithParams(freqList, hiddenNetworkIdList);
    }

    private boolean scanWithParams(String freqList, String hiddenNetworkIdList) {
        StringBuilder scanCommand = new StringBuilder();
        scanCommand.append("SCAN TYPE=ONLY");
        if (freqList != null) {
            scanCommand.append(" freq=").append(freqList);
        }
        if (hiddenNetworkIdList != null) {
            scanCommand.append(" scan_id=").append(hiddenNetworkIdList);
        }
        return doBooleanCommand(scanCommand.toString());
    }

    public boolean stopSupplicant() {
        return doBooleanCommand("TERMINATE");
    }

    public String listNetworks() {
        return doStringCommand("LIST_NETWORKS");
    }

    public String listNetworks(int last_id) {
        return doStringCommand("LIST_NETWORKS LAST_ID=" + last_id);
    }

    public int addNetwork() {
        Log.d(this.mTAG, "addNetwork, mInterfaceName = " + this.mInterfaceName);
        if (this.mInterfaceName.equals("p2p0")) {
            return doIntCommand("IFNAME=" + this.mInterfaceName + " " + "ADD_NETWORK");
        }
        return doIntCommand("ADD_NETWORK");
    }

    public boolean setNetworkExtra(int netId, String name, Map<String, String> values) {
        try {
            return setNetworkVariable(netId, name, "\"" + URLEncoder.encode(new JSONObject(values).toString(), "UTF-8") + "\"");
        } catch (NullPointerException e) {
            Log.e(TAG, "Unable to serialize networkExtra: " + e.toString());
            return false;
        } catch (UnsupportedEncodingException e2) {
            Log.e(TAG, "Unable to serialize networkExtra: " + e2.toString());
            return false;
        }
    }

    public boolean setNetworkVariable(int netId, String name, String value) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            return false;
        }
        if (this.mInterfaceName.equals("p2p0")) {
            return doBooleanCommand(("IFNAME=" + this.mInterfaceName + " ") + "SET_NETWORK " + netId + " " + name + " " + value);
        } else if (name.equals("psk") || name.equals("password")) {
            return doBooleanCommandWithoutLogging("SET_NETWORK " + netId + " " + name + " " + value);
        } else {
            return doBooleanCommand("SET_NETWORK " + netId + " " + name + " " + value);
        }
    }

    public Map<String, String> getNetworkExtra(int netId, String name) {
        String wrapped = getNetworkVariable(netId, name);
        if (wrapped == null || !wrapped.startsWith("\"") || !wrapped.endsWith("\"")) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(URLDecoder.decode(wrapped.substring(1, wrapped.length() - 1), "UTF-8"));
            Map<String, String> values = new HashMap();
            Iterator<?> it = json.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object value = json.get(key);
                if (value instanceof String) {
                    values.put(key, (String) value);
                }
            }
            return values;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unable to deserialize networkExtra: " + e.toString());
            return null;
        } catch (JSONException e2) {
            return null;
        }
    }

    public String getNetworkVariable(int netId, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        return doStringCommandWithoutLogging("GET_NETWORK " + netId + " " + name);
    }

    public boolean removeNetwork(int netId) {
        return doBooleanCommand("REMOVE_NETWORK " + netId);
    }

    private void logDbg(String debug) {
        Object[] objArr = new Object[1];
        objArr[0] = Long.valueOf(SystemClock.elapsedRealtimeNanos() / 1000);
        Log.e("WifiNative: ", String.format("[%,d us] ", objArr) + debug + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName() + " - " + Thread.currentThread().getStackTrace()[6].getMethodName());
    }

    public boolean enableNetwork(int netId) {
        if (DBG) {
            logDbg("enableNetwork nid=" + Integer.toString(netId));
        }
        return doBooleanCommand("ENABLE_NETWORK " + netId);
    }

    public boolean enableNetworkWithoutConnect(int netId) {
        if (DBG) {
            logDbg("enableNetworkWithoutConnect nid=" + Integer.toString(netId));
        }
        return doBooleanCommand("ENABLE_NETWORK " + netId + " " + "no-connect");
    }

    public boolean disableNetwork(int netId) {
        if (DBG) {
            logDbg("disableNetwork nid=" + Integer.toString(netId));
        }
        return doBooleanCommand("DISABLE_NETWORK " + netId);
    }

    public boolean selectNetwork(int netId) {
        if (DBG) {
            logDbg("selectNetwork nid=" + Integer.toString(netId));
        }
        return doBooleanCommand("SELECT_NETWORK " + netId);
    }

    public boolean reconnect() {
        if (DBG) {
            logDbg("RECONNECT ");
        }
        return doBooleanCommand("RECONNECT");
    }

    public boolean manuReassociate() {
        if (DBG) {
            logDbg("manuReassociate ");
        }
        return doBooleanCommand("MANU_REASSOCIATE");
    }

    public boolean reassociate() {
        if (DBG) {
            logDbg("REASSOCIATE ");
        }
        return doBooleanCommand("REASSOCIATE");
    }

    public boolean disconnect() {
        if (DBG) {
            logDbg("DISCONNECT ");
        }
        return doBooleanCommand("DISCONNECT");
    }

    public String status() {
        return status(false);
    }

    public String status(boolean noEvents) {
        if (noEvents) {
            return doStringCommand("STATUS-NO_EVENTS");
        }
        return doStringCommand("STATUS");
    }

    public String getMacAddress() {
        String ret = doStringCommand("DRIVER MACADDR");
        if (!TextUtils.isEmpty(ret)) {
            String[] tokens = ret.split(" = ");
            if (tokens.length == 2) {
                return tokens[1];
            }
        }
        return null;
    }

    private String getRawScanResults(String range) {
        return doStringCommandWithoutLogging("BSS RANGE=" + range + " MASK=0x29d87");
    }

    public ArrayList<ScanDetail> getScanResults() {
        int next_sid = 0;
        ArrayList<ScanDetail> results = new ArrayList();
        while (next_sid >= 0) {
            String rawResult = getRawScanResults(next_sid + "-");
            next_sid = -1;
            if (TextUtils.isEmpty(rawResult)) {
                break;
            }
            String[] lines = rawResult.split("\n");
            int bssidStrLen = BSS_BSSID_STR.length();
            int flagLen = BSS_FLAGS_STR.length();
            String bssid = "";
            int level = 0;
            int freq = 0;
            long tsf = 0;
            String flags = "";
            WifiSsid wifiSsid = null;
            String infoElementsStr = null;
            List anqpLines = null;
            for (String line : lines) {
                if (line.startsWith(BSS_ID_STR)) {
                    try {
                        next_sid = Integer.parseInt(line.substring(BSS_ID_STR.length())) + 1;
                    } catch (NumberFormatException e) {
                    }
                } else if (line.startsWith(BSS_BSSID_STR)) {
                    bssid = new String(line.getBytes(), bssidStrLen, line.length() - bssidStrLen);
                } else if (line.startsWith(BSS_FREQ_STR)) {
                    try {
                        freq = Integer.parseInt(line.substring(BSS_FREQ_STR.length()));
                    } catch (NumberFormatException e2) {
                        freq = 0;
                    }
                } else if (line.startsWith(BSS_LEVEL_STR)) {
                    try {
                        level = Integer.parseInt(line.substring(BSS_LEVEL_STR.length()));
                        if (level > 0) {
                            level -= 256;
                        }
                    } catch (NumberFormatException e3) {
                        level = 0;
                    }
                } else if (line.startsWith(BSS_TSF_STR)) {
                    try {
                        tsf = Long.parseLong(line.substring(BSS_TSF_STR.length()));
                    } catch (NumberFormatException e4) {
                        tsf = 0;
                    }
                } else if (line.startsWith(BSS_FLAGS_STR)) {
                    flags = new String(line.getBytes(), flagLen, line.length() - flagLen);
                } else if (line.startsWith(BSS_SSID_STR)) {
                    wifiSsid = WifiSsid.createFromAsciiEncoded(line.substring(BSS_SSID_STR.length()));
                } else if (line.startsWith(BSS_IE_STR)) {
                    infoElementsStr = line;
                } else if (SupplicantBridge.isAnqpAttribute(line)) {
                    if (anqpLines == null) {
                        anqpLines = new ArrayList();
                    }
                    anqpLines.add(line);
                } else if (line.startsWith(BSS_DELIMITER_STR) || line.startsWith(BSS_END_STR)) {
                    if (bssid != null) {
                        if (infoElementsStr == null) {
                            try {
                                throw new IllegalArgumentException("Null information element data");
                            } catch (IllegalArgumentException iae) {
                                Log.d(TAG, "Failed to parse information elements: " + iae);
                            }
                        } else {
                            int seperator = infoElementsStr.indexOf(61);
                            if (seperator < 0) {
                                throw new IllegalArgumentException("No element separator");
                            }
                            InformationElement[] infoElements = InformationElementUtil.parseInformationElements(Utils.hexToBytes(infoElementsStr.substring(seperator + 1)));
                            NetworkDetail networkDetail = new NetworkDetail(bssid, infoElements, anqpLines, freq);
                            String xssid = wifiSsid != null ? wifiSsid.toString() : "<unknown ssid>";
                            if (!xssid.equals(networkDetail.getTrimmedSSID())) {
                                String str = TAG;
                                Object[] objArr = new Object[4];
                                objArr[0] = bssid;
                                objArr[1] = xssid;
                                objArr[2] = networkDetail.getSSID();
                                objArr[3] = infoElementsStr;
                                Log.d(str, String.format("Inconsistent SSID on BSSID '%s': '%s' vs '%s': %s", objArr));
                            }
                            if (networkDetail.hasInterworking() && DBG) {
                                Log.d(TAG, "HSNwk: '" + networkDetail);
                            }
                            if (level >= OPPO_POOR_RSSI) {
                                results.add(new ScanDetail(networkDetail, wifiSsid, bssid, flags, level, freq, tsf, infoElements, anqpLines));
                            } else if (DBG) {
                                Log.d(TAG, "Remove poor signal APs, SSID=" + wifiSsid + ", bssid=" + bssid + ", freq=" + freq + ", level=" + level + ", flags=" + flags);
                            }
                        }
                    }
                    bssid = null;
                    level = 0;
                    freq = 0;
                    tsf = 0;
                    flags = "";
                    wifiSsid = null;
                    infoElementsStr = null;
                    anqpLines = null;
                }
            }
        }
        return results;
    }

    public String scanResult(String bssid) {
        return doStringCommand("BSS " + bssid);
    }

    public boolean startDriver() {
        return doBooleanCommand("DRIVER START");
    }

    public boolean stopDriver() {
        return doBooleanCommand("DRIVER STOP");
    }

    public boolean startFilteringMulticastV4Packets() {
        if (doBooleanCommand("DRIVER RXFILTER-STOP") && doBooleanCommand("DRIVER RXFILTER-REMOVE 2")) {
            return doBooleanCommand("DRIVER RXFILTER-START");
        }
        return false;
    }

    public boolean stopFilteringMulticastV4Packets() {
        if (doBooleanCommand("DRIVER RXFILTER-STOP") && doBooleanCommand("DRIVER RXFILTER-ADD 2")) {
            return doBooleanCommand("DRIVER RXFILTER-START");
        }
        return false;
    }

    public boolean startFilteringMulticastV6Packets() {
        if (doBooleanCommand("DRIVER RXFILTER-STOP") && doBooleanCommand("DRIVER RXFILTER-REMOVE 3")) {
            return doBooleanCommand("DRIVER RXFILTER-START");
        }
        return false;
    }

    public boolean stopFilteringMulticastV6Packets() {
        if (doBooleanCommand("DRIVER RXFILTER-STOP") && doBooleanCommand("DRIVER RXFILTER-ADD 3")) {
            return doBooleanCommand("DRIVER RXFILTER-START");
        }
        return false;
    }

    public boolean setBand(int band) {
        String bandstr;
        if (band == 1) {
            bandstr = "5G";
        } else if (band == 2) {
            bandstr = "2G";
        } else {
            bandstr = "AUTO";
        }
        return doBooleanCommand("SET SETBAND " + bandstr);
    }

    public boolean setBluetoothCoexistenceMode(int mode) {
        return doBooleanCommand("DRIVER BTCOEXMODE " + mode);
    }

    public boolean setBluetoothCoexistenceScanMode(boolean setCoexScanMode) {
        if (setCoexScanMode) {
            return doBooleanCommand("DRIVER BTCOEXSCAN-START");
        }
        return doBooleanCommand("DRIVER BTCOEXSCAN-STOP");
    }

    public void enableSaveConfig() {
        doBooleanCommand("SET update_config 1");
    }

    public boolean saveConfig() {
        return doBooleanCommand("SAVE_CONFIG");
    }

    public boolean addToBlacklist(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return false;
        }
        return doBooleanCommand("BLACKLIST " + bssid);
    }

    public boolean clearBlacklist() {
        return doBooleanCommand("BLACKLIST clear");
    }

    public boolean setSuspendOptimizations(boolean enabled) {
        if (enabled) {
            return doBooleanCommand("DRIVER SETSUSPENDMODE 1");
        }
        return doBooleanCommand("DRIVER SETSUSPENDMODE 0");
    }

    public boolean setCountryCode(String countryCode) {
        if (countryCode != null) {
            return doBooleanCommand("DRIVER COUNTRY " + countryCode.toUpperCase(Locale.ROOT));
        }
        return doBooleanCommand("DRIVER COUNTRY");
    }

    public boolean setPnoScan(boolean enable) {
        return doBooleanCommand(enable ? "SET pno 1" : "SET pno 0");
    }

    public void enableAutoConnect(boolean enable) {
        if (enable) {
            doBooleanCommand("STA_AUTOCONNECT 1");
        } else {
            doBooleanCommand("STA_AUTOCONNECT 0");
        }
    }

    public void setScanInterval(int scanInterval) {
        doBooleanCommand("SCAN_INTERVAL " + scanInterval);
    }

    public void setHs20(boolean hs20) {
        if (hs20) {
            doBooleanCommand("SET HS20 1");
        } else {
            doBooleanCommand("SET HS20 0");
        }
    }

    public void startTdls(String macAddr, boolean enable) {
        if (enable) {
            synchronized (sLock) {
                doBooleanCommand("TDLS_DISCOVER " + macAddr);
                doBooleanCommand("TDLS_SETUP " + macAddr);
            }
            return;
        }
        doBooleanCommand("TDLS_TEARDOWN " + macAddr);
    }

    public String signalPoll() {
        return doStringCommandWithoutLogging("SIGNAL_POLL");
    }

    public String pktcntPoll() {
        return doStringCommand("PKTCNT_POLL");
    }

    public void bssFlush() {
        if (this.mInterfaceName.equals("p2p0")) {
            doBooleanCommand("IFNAME=" + this.mInterfaceName + " BSS_FLUSH 0");
        } else {
            doBooleanCommand("BSS_FLUSH 0");
        }
    }

    public boolean startWpsPbc(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return doBooleanCommand("WPS_PBC");
        }
        return doBooleanCommand("WPS_PBC " + bssid);
    }

    public boolean startWpsPbc(String iface, String bssid) {
        synchronized (sLock) {
            boolean doBooleanCommandNative;
            if (TextUtils.isEmpty(bssid)) {
                doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " WPS_PBC");
                return doBooleanCommandNative;
            }
            doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " WPS_PBC " + bssid);
            return doBooleanCommandNative;
        }
    }

    public boolean startWpsPinKeypad(String pin) {
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        return doBooleanCommand("WPS_PIN any " + pin);
    }

    public boolean startWpsPinKeypad(String iface, String pin) {
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        boolean doBooleanCommandNative;
        synchronized (sLock) {
            doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " WPS_PIN any " + pin);
        }
        return doBooleanCommandNative;
    }

    public String startWpsPinDisplay(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return doStringCommand("WPS_PIN any");
        }
        return doStringCommand("WPS_PIN " + bssid);
    }

    public String startWpsPinDisplay(String iface, String bssid) {
        synchronized (sLock) {
            String doStringCommandNative;
            if (TextUtils.isEmpty(bssid)) {
                doStringCommandNative = doStringCommandNative("IFNAME=" + iface + " WPS_PIN any");
                return doStringCommandNative;
            }
            doStringCommandNative = doStringCommandNative("IFNAME=" + iface + " WPS_PIN " + bssid);
            return doStringCommandNative;
        }
    }

    public boolean setExternalSim(boolean external) {
        String value = external ? "1" : "0";
        Log.d(TAG, "Setting external_sim to " + value);
        return doBooleanCommand("SET external_sim " + value);
    }

    public boolean simAuthResponse(int id, String type, String response) {
        return doBooleanCommand("CTRL-RSP-SIM-" + id + ":" + type + response);
    }

    public boolean simAuthFailedResponse(int id) {
        return doBooleanCommand("CTRL-RSP-SIM-" + id + ":GSM-FAIL");
    }

    public boolean umtsAuthFailedResponse(int id) {
        return doBooleanCommand("CTRL-RSP-SIM-" + id + ":UMTS-FAIL");
    }

    public boolean simIdentityResponse(int id, String response) {
        return doBooleanCommand("CTRL-RSP-IDENTITY-" + id + ":" + response);
    }

    public boolean startWpsRegistrar(String bssid, String pin) {
        if (TextUtils.isEmpty(bssid) || TextUtils.isEmpty(pin)) {
            return false;
        }
        return doBooleanCommand("WPS_REG " + bssid + " " + pin);
    }

    public boolean cancelWps() {
        return doBooleanCommand("WPS_CANCEL");
    }

    public boolean setPersistentReconnect(boolean enabled) {
        return doBooleanCommand("SET persistent_reconnect " + (enabled ? 1 : 0));
    }

    public boolean setDeviceName(String name) {
        return doBooleanCommand("SET device_name " + name);
    }

    public boolean setDeviceType(String type) {
        return doBooleanCommand("SET device_type " + type);
    }

    public boolean setConfigMethods(String cfg) {
        return doBooleanCommand("SET config_methods " + cfg);
    }

    public boolean setManufacturer(String value) {
        return doBooleanCommand("SET manufacturer " + value);
    }

    public boolean setModelName(String value) {
        return doBooleanCommand("SET model_name " + value);
    }

    public boolean setModelNumber(String value) {
        return doBooleanCommand("SET model_number " + value);
    }

    public boolean setSerialNumber(String value) {
        return doBooleanCommand("SET serial_number " + value);
    }

    public boolean setP2pSsidPostfix(String postfix) {
        return doBooleanCommand("SET p2p_ssid_postfix " + postfix);
    }

    public boolean setP2pGroupIdle(String iface, int time) {
        boolean doBooleanCommandNative;
        synchronized (sLock) {
            doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " SET p2p_group_idle " + time);
        }
        return doBooleanCommandNative;
    }

    public void setPowerSave(boolean enabled) {
        if (enabled) {
            doBooleanCommand("SET ps 1");
        } else {
            doBooleanCommand("SET ps 0");
        }
    }

    public boolean setP2pPowerSave(String iface, boolean enabled) {
        synchronized (sLock) {
            boolean doBooleanCommandNative;
            if (enabled) {
                doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " P2P_SET ps 1");
                return doBooleanCommandNative;
            }
            doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " P2P_SET ps 0");
            return doBooleanCommandNative;
        }
    }

    public boolean setWfdEnable(boolean enable) {
        return doBooleanCommand("SET wifi_display " + (enable ? "1" : "0"));
    }

    public boolean setWfdDeviceInfo(String hex) {
        return doBooleanCommand("WFD_SUBELEM_SET 0 " + hex);
    }

    public boolean setConcurrencyPriority(String s) {
        return doBooleanCommand("P2P_SET conc_pref " + s);
    }

    public boolean p2pFind() {
        return doBooleanCommand("P2P_FIND type=progressive");
    }

    public boolean p2pFind(int timeout) {
        if (timeout <= 0) {
            return p2pFind();
        }
        return doBooleanCommand("P2P_FIND " + timeout + " type=progressive");
    }

    public boolean p2pStopFind() {
        return doBooleanCommand("P2P_STOP_FIND");
    }

    public boolean p2pListen() {
        return doBooleanCommand("P2P_LISTEN");
    }

    public boolean p2pListen(int timeout) {
        if (timeout <= 0) {
            return p2pListen();
        }
        return doBooleanCommand("P2P_LISTEN " + timeout);
    }

    public boolean p2pExtListen(boolean enable, int period, int interval) {
        if (enable && interval < period) {
            return false;
        }
        return doBooleanCommand("P2P_EXT_LISTEN" + (enable ? " " + period + " " + interval : ""));
    }

    public boolean p2pSetChannel(int lc, int oc) {
        if (DBG) {
            Log.d(this.mTAG, "p2pSetChannel: lc=" + lc + ", oc=" + oc);
        }
        synchronized (sLock) {
            if (lc < 1 || lc > 11) {
                if (lc != 0) {
                    return false;
                }
            } else if (!doBooleanCommand("P2P_SET listen_channel " + lc)) {
                return false;
            }
            boolean doBooleanCommand;
            if (oc >= 1 && oc <= 165) {
                int freq = (oc <= 14 ? 2407 : 5000) + (oc * 5);
                doBooleanCommand = doBooleanCommand("P2P_SET disallow_freq 1000-" + (freq - 5) + "," + (freq + 5) + "-6000");
                return doBooleanCommand;
            } else if (oc == 0) {
                doBooleanCommand = doBooleanCommand("P2P_SET disallow_freq \"\"");
                return doBooleanCommand;
            } else {
                return false;
            }
        }
    }

    public boolean p2pFlush() {
        return doBooleanCommand("P2P_FLUSH");
    }

    public String p2pConnect(WifiP2pConfig config, boolean joinExistingGroup) {
        if (config == null) {
            return null;
        }
        List<String> args = new ArrayList();
        WpsInfo wps = config.wps;
        args.add(config.deviceAddress);
        switch (wps.setup) {
            case 0:
                args.add("pbc");
                break;
            case 1:
                if (TextUtils.isEmpty(wps.pin)) {
                    args.add("pin");
                } else {
                    args.add(wps.pin);
                }
                args.add("display");
                break;
            case 2:
                args.add(wps.pin);
                args.add("keypad");
                break;
            case 3:
                args.add(wps.pin);
                args.add("label");
                break;
        }
        if (config.netId == -2) {
            args.add("persistent");
        }
        if (joinExistingGroup) {
            args.add("join");
        } else {
            int groupOwnerIntent = config.groupOwnerIntent;
            if (groupOwnerIntent < 0 || groupOwnerIntent > 15) {
                groupOwnerIntent = 6;
            }
            args.add("go_intent=" + groupOwnerIntent);
        }
        int preferOperFreq = config.getPreferOperFreq();
        if (-1 != preferOperFreq) {
            args.add(BSS_FREQ_STR + preferOperFreq);
        }
        String command = "P2P_CONNECT ";
        for (String s : args) {
            command = command + s + " ";
        }
        return doStringCommand(command);
    }

    public String p2pConnect(WifiP2pConfig config, boolean joinExistingGroup, int freq) {
        if (config == null) {
            return null;
        }
        List<String> args = new ArrayList();
        WpsInfo wps = config.wps;
        args.add(config.deviceAddress);
        switch (wps.setup) {
            case 0:
                args.add("pbc");
                break;
            case 1:
                if (TextUtils.isEmpty(wps.pin)) {
                    args.add("pin");
                } else {
                    args.add(wps.pin);
                }
                args.add("display");
                break;
            case 2:
                args.add(wps.pin);
                args.add("keypad");
                break;
            case 3:
                args.add(wps.pin);
                args.add("label");
                break;
        }
        if (config.netId == -2) {
            args.add("persistent");
        }
        if (joinExistingGroup) {
            args.add("join");
        } else {
            int groupOwnerIntent = config.groupOwnerIntent;
            if (groupOwnerIntent < 0 || groupOwnerIntent > 15) {
                groupOwnerIntent = 6;
            }
            args.add("go_intent=" + groupOwnerIntent);
        }
        if (freq <= 0) {
            args.add("freq=5745");
        } else {
            args.add(BSS_FREQ_STR + freq);
        }
        String command = "P2P_CONNECT ";
        for (String s : args) {
            command = command + s + " ";
        }
        return doStringCommand(command);
    }

    public boolean p2pFind(int timeout, int freq) {
        p2pStopFind();
        return doBooleanCommand("P2P_FIND " + timeout + " freq=" + freq);
    }

    public boolean p2pCancelConnect() {
        return doBooleanCommand("P2P_CANCEL");
    }

    public boolean p2pProvisionDiscovery(WifiP2pConfig config) {
        if (config == null) {
            return false;
        }
        switch (config.wps.setup) {
            case 0:
                return doBooleanCommand("P2P_PROV_DISC " + config.deviceAddress + " pbc");
            case 1:
                return doBooleanCommand("P2P_PROV_DISC " + config.deviceAddress + " keypad");
            case 2:
                return doBooleanCommand("P2P_PROV_DISC " + config.deviceAddress + " display");
            default:
                return false;
        }
    }

    public boolean p2pGroupAdd(boolean persistent) {
        if (persistent) {
            return doBooleanCommand("P2P_GROUP_ADD persistent");
        }
        return doBooleanCommand("P2P_GROUP_ADD");
    }

    public boolean p2pGroupAdd(int netId) {
        return doBooleanCommand("P2P_GROUP_ADD persistent=" + netId);
    }

    public boolean p2pGroupRemove(String iface) {
        if (TextUtils.isEmpty(iface)) {
            return false;
        }
        boolean doBooleanCommandNative;
        synchronized (sLock) {
            doBooleanCommandNative = doBooleanCommandNative("IFNAME=" + iface + " P2P_GROUP_REMOVE " + iface);
        }
        return doBooleanCommandNative;
    }

    public boolean p2pReject(String deviceAddress) {
        return doBooleanCommand("P2P_REJECT " + deviceAddress);
    }

    public boolean p2pInvite(WifiP2pGroup group, String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            return false;
        }
        if (group == null) {
            return doBooleanCommand("P2P_INVITE peer=" + deviceAddress);
        }
        return doBooleanCommand("P2P_INVITE group=" + group.getInterface() + " peer=" + deviceAddress + " go_dev_addr=" + group.getOwner().deviceAddress);
    }

    public boolean p2pReinvoke(int netId, String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress) || netId < 0) {
            return false;
        }
        bssFlush();
        return doBooleanCommand("P2P_INVITE persistent=" + netId + " peer=" + deviceAddress);
    }

    public String p2pGetSsid(String deviceAddress) {
        return p2pGetParam(deviceAddress, "oper_ssid");
    }

    public String p2pGetDeviceAddress() {
        String status;
        Log.d(TAG, "p2pGetDeviceAddress");
        synchronized (sLock) {
            status = doStringCommandNative("STATUS");
        }
        String result = "";
        if (status != null) {
            for (String token : status.split("\n")) {
                if (token.startsWith("p2p_device_address=")) {
                    String[] nameValue = token.split("=");
                    if (nameValue.length != 2) {
                        break;
                    }
                    result = nameValue[1];
                }
            }
        }
        Log.d(TAG, "p2pGetDeviceAddress returning " + result);
        return result;
    }

    public int getGroupCapability(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            return 0;
        }
        String peerInfo = p2pPeer(deviceAddress);
        if (TextUtils.isEmpty(peerInfo)) {
            return 0;
        }
        for (String token : peerInfo.split("\n")) {
            if (token.startsWith("group_capab=")) {
                String[] nameValue = token.split("=");
                if (nameValue.length == 2) {
                    try {
                        return Integer.decode(nameValue[1]).intValue();
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
                return 0;
            }
        }
        return 0;
    }

    public String p2pPeer(String deviceAddress) {
        return doStringCommand("P2P_PEER " + deviceAddress);
    }

    private String p2pGetParam(String deviceAddress, String key) {
        if (deviceAddress == null) {
            return null;
        }
        String peerInfo = p2pPeer(deviceAddress);
        if (peerInfo == null) {
            return null;
        }
        key = key + "=";
        for (String token : peerInfo.split("\n")) {
            if (token.startsWith(key)) {
                String[] nameValue = token.split("=");
                if (nameValue.length == 2) {
                    return nameValue[1];
                }
                return null;
            }
        }
        return null;
    }

    public boolean p2pServiceAdd(WifiP2pServiceInfo servInfo) {
        synchronized (sLock) {
            for (String s : servInfo.getSupplicantQueryList()) {
                if (!doBooleanCommand("P2P_SERVICE_ADD" + " " + s)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean p2pServiceDel(WifiP2pServiceInfo servInfo) {
        synchronized (sLock) {
            for (String s : servInfo.getSupplicantQueryList()) {
                String command = "P2P_SERVICE_DEL ";
                String[] data = s.split(" ");
                if (data.length < 2) {
                    return false;
                }
                if ("upnp".equals(data[0])) {
                    command = command + s;
                } else if ("bonjour".equals(data[0])) {
                    command = (command + data[0]) + " " + data[1];
                } else {
                    return false;
                }
                if (!doBooleanCommand(command)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean p2pServiceFlush() {
        return doBooleanCommand("P2P_SERVICE_FLUSH");
    }

    public String p2pServDiscReq(String addr, String query) {
        return doStringCommand(("P2P_SERV_DISC_REQ" + " " + addr) + " " + query);
    }

    public boolean p2pServDiscCancelReq(String id) {
        return doBooleanCommand("P2P_SERV_DISC_CANCEL_REQ " + id);
    }

    public void setMiracastMode(int mode) {
        doBooleanCommand("DRIVER MIRACAST " + mode);
    }

    public void setMiracastMode(int mode, int freq) {
        doBooleanCommand("DRIVER MIRACAST " + mode + " freq=" + freq);
    }

    public boolean fetchAnqp(String bssid, String subtypes) {
        return doBooleanCommand("ANQP_GET " + bssid + " " + subtypes);
    }

    public String getNfcWpsConfigurationToken(int netId) {
        return doStringCommand("WPS_NFC_CONFIG_TOKEN WPS " + netId);
    }

    public String getNfcHandoverRequest() {
        return doStringCommand("NFC_GET_HANDOVER_REQ NDEF P2P-CR");
    }

    public String getNfcHandoverSelect() {
        return doStringCommand("NFC_GET_HANDOVER_SEL NDEF P2P-CR");
    }

    public boolean initiatorReportNfcHandover(String selectMessage) {
        return doBooleanCommand("NFC_REPORT_HANDOVER INIT P2P 00 " + selectMessage);
    }

    public boolean responderReportNfcHandover(String requestMessage) {
        return doBooleanCommand("NFC_REPORT_HANDOVER RESP P2P " + requestMessage + " 00");
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

    private static int getNewCmdIdLocked() {
        int i = sCmdId;
        sCmdId = i + 1;
        return i;
    }

    /* JADX WARNING: Missing block: B:17:0x006b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startHal() {
        String debugLog = "startHal stack: ";
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int i = 2;
        while (i < elements.length && i <= 7) {
            debugLog = debugLog + " - " + elements[i].getMethodName();
            i++;
        }
        sLocalLog.log(debugLog);
        synchronized (sLock) {
            if (startHalNative()) {
                int wlan0Index = queryInterfaceIndex(this.mInterfaceName);
                if (wlan0Index != -1) {
                    sWlan0Index = wlan0Index;
                    sThread = new MonitorThread();
                    sThread.start();
                    return true;
                } else if (DBG) {
                    sLocalLog.log("Could not find interface with name: " + this.mInterfaceName);
                }
            } else {
                if (DBG) {
                    sLocalLog.log("Could not start hal");
                }
                Log.e(TAG, "Could not start hal");
                return false;
            }
        }
    }

    public void stopHal() {
        synchronized (sLock) {
            if (isHalStarted()) {
                stopHalNative();
                try {
                    sThread.join(1000);
                    Log.d(TAG, "HAL event thread stopped successfully");
                } catch (InterruptedException e) {
                    Log.e(TAG, "Could not stop HAL cleanly");
                }
                sThread = null;
                sWifiHalHandle = 0;
                sWifiIfaceHandles = null;
                sWlan0Index = -1;
            }
        }
        return;
    }

    public boolean isHalStarted() {
        return sWifiHalHandle != 0;
    }

    /* JADX WARNING: Missing block: B:14:0x0021, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryInterfaceIndex(String interfaceName) {
        synchronized (sLock) {
            if (isHalStarted()) {
                int num = getInterfacesNative();
                for (int i = 0; i < num; i++) {
                    if (getInterfaceNameNative(i).equals(interfaceName)) {
                        return i;
                    }
                }
            }
        }
    }

    public String getInterfaceName(int index) {
        String interfaceNameNative;
        synchronized (sLock) {
            interfaceNameNative = getInterfaceNameNative(index);
        }
        return interfaceNameNative;
    }

    public boolean getScanCapabilities(ScanCapabilities capabilities) {
        boolean scanCapabilitiesNative;
        synchronized (sLock) {
            scanCapabilitiesNative = isHalStarted() ? getScanCapabilitiesNative(sWlan0Index, capabilities) : false;
        }
        return scanCapabilitiesNative;
    }

    private static void onScanStatus(int id, int event) {
        ScanEventHandler handler = sScanEventHandler;
        if (handler != null) {
            handler.onScanStatus(event);
        }
    }

    public static WifiSsid createWifiSsid(byte[] rawSsid) {
        String ssidHexString = String.valueOf(HexEncoding.encode(rawSsid));
        if (ssidHexString == null) {
            return null;
        }
        return WifiSsid.createFromHex(ssidHexString);
    }

    public static String ssidConvert(byte[] rawSsid) {
        String ssid;
        try {
            ssid = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(rawSsid)).toString();
        } catch (CharacterCodingException e) {
            ssid = null;
        }
        if (ssid == null) {
            return new String(rawSsid, StandardCharsets.ISO_8859_1);
        }
        return ssid;
    }

    public static boolean setSsid(byte[] rawSsid, ScanResult result) {
        if (rawSsid == null || rawSsid.length == 0 || result == null) {
            return false;
        }
        result.SSID = ssidConvert(rawSsid);
        result.wifiSsid = createWifiSsid(rawSsid);
        return true;
    }

    private static void populateScanResult(ScanResult result, int beaconCap, String dbg) {
        if (dbg == null) {
            dbg = "";
        }
        HtOperation htOperation = new HtOperation();
        VhtOperation vhtOperation = new VhtOperation();
        ExtendedCapabilities extendedCaps = new ExtendedCapabilities();
        InformationElement[] elements = InformationElementUtil.parseInformationElements(result.bytes);
        for (InformationElement ie : elements) {
            if (ie.id == 61) {
                htOperation.from(ie);
            } else if (ie.id == 192) {
                vhtOperation.from(ie);
            } else if (ie.id == SupportedRates.MASK) {
                extendedCaps.from(ie);
            }
        }
        if (extendedCaps.is80211McRTTResponder) {
            result.setFlag(2);
        } else {
            result.clearFlag(2);
        }
        if (vhtOperation.isValid()) {
            result.channelWidth = vhtOperation.getChannelWidth();
            result.centerFreq0 = vhtOperation.getCenterFreq0();
            result.centerFreq1 = vhtOperation.getCenterFreq1();
        } else {
            result.channelWidth = htOperation.getChannelWidth();
            result.centerFreq0 = htOperation.getCenterFreq0(result.frequency);
            result.centerFreq1 = 0;
        }
        BitSet beaconCapBits = new BitSet(16);
        for (int i = 0; i < 16; i++) {
            if (((1 << i) & beaconCap) != 0) {
                beaconCapBits.set(i);
            }
        }
        result.capabilities = Capabilities.buildCapabilities(elements, beaconCapBits);
        if (DBG) {
            Log.d(TAG, dbg + "SSID: " + result.SSID + " ChannelWidth is: " + result.channelWidth + " PrimaryFreq: " + result.frequency + " mCenterfreq0: " + result.centerFreq0 + " mCenterfreq1: " + result.centerFreq1 + (extendedCaps.is80211McRTTResponder ? "Support RTT reponder: " : "Do not support RTT responder") + " Capabilities: " + result.capabilities);
        }
        result.informationElements = elements;
    }

    private static void onFullScanResult(int id, ScanResult result, int bucketsScanned, int beaconCap) {
        if (DBG) {
            Log.i(TAG, "Got a full scan results event, ssid = " + result.SSID);
        }
        ScanEventHandler handler = sScanEventHandler;
        if (handler != null) {
            populateScanResult(result, beaconCap, " onFullScanResult ");
            handler.onFullScanResult(result, bucketsScanned);
        }
    }

    public boolean startScan(ScanSettings settings, ScanEventHandler eventHandler) {
        synchronized (sLock) {
            if (isHalStarted()) {
                if (sScanCmdId != 0) {
                    stopScan();
                } else if (!(sScanSettings == null && sScanEventHandler == null)) {
                }
                sScanCmdId = getNewCmdIdLocked();
                sScanSettings = settings;
                sScanEventHandler = eventHandler;
                if (startScanNative(sWlan0Index, sScanCmdId, settings)) {
                    return true;
                }
                sScanEventHandler = null;
                sScanSettings = null;
                sScanCmdId = 0;
                return false;
            }
            return false;
        }
    }

    public void stopScan() {
        synchronized (sLock) {
            if (isHalStarted()) {
                if (sScanCmdId != 0) {
                    stopScanNative(sWlan0Index, sScanCmdId);
                }
                sScanSettings = null;
                sScanEventHandler = null;
                sScanCmdId = 0;
            }
        }
    }

    public void pauseScan() {
        synchronized (sLock) {
            if (!(!isHalStarted() || sScanCmdId == 0 || sScanSettings == null || sScanEventHandler == null)) {
                Log.d(TAG, "Pausing scan");
                ScanData[] scanData = getScanResultsNative(sWlan0Index, true);
                stopScanNative(sWlan0Index, sScanCmdId);
                sScanCmdId = 0;
                sScanEventHandler.onScanPaused(scanData);
            }
        }
    }

    public void restartScan() {
        synchronized (sLock) {
            if (isHalStarted() && sScanCmdId == 0 && sScanSettings != null && sScanEventHandler != null) {
                Log.d(TAG, "Restarting scan");
                ScanEventHandler handler = sScanEventHandler;
                ScanSettings settings = sScanSettings;
                if (startScan(sScanSettings, sScanEventHandler)) {
                    sScanEventHandler.onScanRestarted();
                } else {
                    sScanEventHandler = handler;
                    sScanSettings = settings;
                }
            }
        }
    }

    public ScanData[] getScanResults(boolean flush) {
        synchronized (sLock) {
            ScanData[] sd = null;
            if (isHalStarted()) {
                sd = getScanResultsNative(sWlan0Index, flush);
            }
            if (sd != null) {
                return sd;
            }
            ScanData[] scanDataArr = new ScanData[0];
            return scanDataArr;
        }
    }

    public boolean setHotlist(HotlistSettings settings, HotlistEventHandler eventHandler) {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (sHotlistCmdId != 0) {
                return false;
            } else {
                sHotlistCmdId = getNewCmdIdLocked();
                sHotlistEventHandler = eventHandler;
                if (setHotlistNative(sWlan0Index, sHotlistCmdId, settings)) {
                    return true;
                }
                sHotlistEventHandler = null;
                return false;
            }
        }
    }

    public void resetHotlist() {
        synchronized (sLock) {
            if (isHalStarted() && sHotlistCmdId != 0) {
                resetHotlistNative(sWlan0Index, sHotlistCmdId);
                sHotlistCmdId = 0;
                sHotlistEventHandler = null;
            }
        }
    }

    private static void onHotlistApFound(int id, ScanResult[] results) {
        HotlistEventHandler handler = sHotlistEventHandler;
        if (handler != null) {
            handler.onHotlistApFound(results);
        } else {
            Log.d(TAG, "Ignoring hotlist AP found event");
        }
    }

    private static void onHotlistApLost(int id, ScanResult[] results) {
        HotlistEventHandler handler = sHotlistEventHandler;
        if (handler != null) {
            handler.onHotlistApLost(results);
        } else {
            Log.d(TAG, "Ignoring hotlist AP lost event");
        }
    }

    public boolean trackSignificantWifiChange(WifiChangeSettings settings, SignificantWifiChangeEventHandler handler) {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (sSignificantWifiChangeCmdId != 0) {
                return false;
            } else {
                sSignificantWifiChangeCmdId = getNewCmdIdLocked();
                sSignificantWifiChangeHandler = handler;
                if (trackSignificantWifiChangeNative(sWlan0Index, sSignificantWifiChangeCmdId, settings)) {
                    return true;
                }
                sSignificantWifiChangeHandler = null;
                return false;
            }
        }
    }

    public void untrackSignificantWifiChange() {
        synchronized (sLock) {
            if (isHalStarted() && sSignificantWifiChangeCmdId != 0) {
                untrackSignificantWifiChangeNative(sWlan0Index, sSignificantWifiChangeCmdId);
                sSignificantWifiChangeCmdId = 0;
                sSignificantWifiChangeHandler = null;
            }
        }
    }

    private static void onSignificantWifiChange(int id, ScanResult[] results) {
        SignificantWifiChangeEventHandler handler = sSignificantWifiChangeHandler;
        if (handler != null) {
            handler.onChangesFound(results);
        } else {
            Log.d(TAG, "Ignoring significant wifi change");
        }
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String iface) {
        if (iface == null) {
            return null;
        }
        synchronized (sLock) {
            if (isHalStarted()) {
                WifiLinkLayerStats wifiLinkLayerStatsNative = getWifiLinkLayerStatsNative(sWlan0Index);
                return wifiLinkLayerStatsNative;
            }
            return null;
        }
    }

    public void setWifiLinkLayerStats(String iface, int enable) {
        if (iface != null) {
            synchronized (sLock) {
                if (isHalStarted()) {
                    setWifiLinkLayerStatsNative(sWlan0Index, enable);
                }
            }
        }
    }

    public int getSupportedFeatureSet() {
        synchronized (sLock) {
            if (isHalStarted()) {
                int supportedFeatureSetNative = getSupportedFeatureSetNative(sWlan0Index);
                return supportedFeatureSetNative;
            }
            Log.d(TAG, "Failing getSupportedFeatureset because HAL isn't started");
            return 0;
        }
    }

    private static void onRttResults(int id, RttResult[] results) {
        RttEventHandler handler = sRttEventHandler;
        if (handler == null || id != sRttCmdId) {
            Log.d(TAG, "RTT Received event for unknown cmd = " + id + ", current id = " + sRttCmdId);
            return;
        }
        Log.d(TAG, "Received " + results.length + " rtt results");
        handler.onRttResults(results);
        sRttCmdId = 0;
    }

    public boolean requestRtt(RttParams[] params, RttEventHandler handler) {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (sRttCmdId != 0) {
                Log.w(TAG, "Last one is still under measurement!");
                return false;
            } else {
                sRttCmdId = getNewCmdIdLocked();
                sRttEventHandler = handler;
                boolean requestRangeNative = requestRangeNative(sWlan0Index, sRttCmdId, params);
                return requestRangeNative;
            }
        }
    }

    public boolean cancelRtt(RttParams[] params) {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (sRttCmdId == 0) {
                return false;
            } else {
                sRttCmdId = 0;
                if (cancelRangeRequestNative(sWlan0Index, sRttCmdId, params)) {
                    sRttEventHandler = null;
                    return true;
                }
                Log.e(TAG, "RTT cancel Request failed");
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x001e, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:26:0x0050, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ResponderConfig enableRttResponder(int timeoutSeconds) {
        boolean z = false;
        synchronized (sLock) {
            if (!isHalStarted()) {
                return null;
            } else if (sRttResponderCmdId == 0) {
                int id = getNewCmdIdLocked();
                ResponderConfig config = enableRttResponderNative(sWlan0Index, id, timeoutSeconds, null);
                if (config != null) {
                    sRttResponderCmdId = id;
                }
                if (DBG) {
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("enabling rtt ");
                    if (config != null) {
                        z = true;
                    }
                    Log.d(str, append.append(z).toString());
                }
            } else if (DBG) {
                Log.e(this.mTAG, "responder mode already enabled - this shouldn't happen");
            }
        }
    }

    public boolean disableRttResponder() {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (sRttResponderCmdId == 0) {
                Log.e(this.mTAG, "responder role not enabled yet");
                return true;
            } else {
                sRttResponderCmdId = 0;
                boolean disableRttResponderNative = disableRttResponderNative(sWlan0Index, sRttResponderCmdId);
                return disableRttResponderNative;
            }
        }
    }

    public boolean setScanningMacOui(byte[] oui) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean scanningMacOuiNative = setScanningMacOuiNative(sWlan0Index, oui);
                return scanningMacOuiNative;
            }
            return false;
        }
    }

    public int[] getChannelsForBand(int band) {
        synchronized (sLock) {
            if (isHalStarted()) {
                int[] channelsForBandNative = getChannelsForBandNative(sWlan0Index, band);
                return channelsForBandNative;
            }
            return null;
        }
    }

    public boolean isGetChannelsForBandSupported() {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean isGetChannelsForBandSupportedNative = isGetChannelsForBandSupportedNative();
                return isGetChannelsForBandSupportedNative;
            }
            return false;
        }
    }

    public boolean setDfsFlag(boolean dfsOn) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean dfsFlagNative = setDfsFlagNative(sWlan0Index, dfsOn);
                return dfsFlagNative;
            }
            return false;
        }
    }

    public boolean setInterfaceUp(boolean up) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean interfaceUpNative = setInterfaceUpNative(up);
                return interfaceUpNative;
            }
            return false;
        }
    }

    public RttCapabilities getRttCapabilities() {
        synchronized (sLock) {
            if (isHalStarted()) {
                RttCapabilities rttCapabilitiesNative = getRttCapabilitiesNative(sWlan0Index);
                return rttCapabilitiesNative;
            }
            return null;
        }
    }

    public ApfCapabilities getApfCapabilities() {
        synchronized (sLock) {
            if (isHalStarted()) {
                ApfCapabilities apfCapabilitiesNative = getApfCapabilitiesNative(sWlan0Index);
                return apfCapabilitiesNative;
            }
            return null;
        }
    }

    public boolean installPacketFilter(byte[] filter) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean installPacketFilterNative = installPacketFilterNative(sWlan0Index, filter);
                return installPacketFilterNative;
            }
            return false;
        }
    }

    public boolean setCountryCodeHal(String CountryCode) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean countryCodeHalNative = setCountryCodeHalNative(sWlan0Index, CountryCode);
                return countryCodeHalNative;
            }
            return false;
        }
    }

    public boolean enableDisableTdls(boolean enable, String macAdd, TdlsEventHandler tdlsCallBack) {
        boolean enableDisableTdlsNative;
        synchronized (sLock) {
            sTdlsEventHandler = tdlsCallBack;
            enableDisableTdlsNative = enableDisableTdlsNative(sWlan0Index, enable, macAdd);
        }
        return enableDisableTdlsNative;
    }

    public TdlsStatus getTdlsStatus(String macAdd) {
        synchronized (sLock) {
            if (isHalStarted()) {
                TdlsStatus tdlsStatusNative = getTdlsStatusNative(sWlan0Index, macAdd);
                return tdlsStatusNative;
            }
            return null;
        }
    }

    public TdlsCapabilities getTdlsCapabilities() {
        synchronized (sLock) {
            if (isHalStarted()) {
                TdlsCapabilities tdlsCapabilitiesNative = getTdlsCapabilitiesNative(sWlan0Index);
                return tdlsCapabilitiesNative;
            }
            return null;
        }
    }

    private static boolean onTdlsStatus(String macAddr, int status, int reason) {
        TdlsEventHandler handler = sTdlsEventHandler;
        if (handler == null) {
            return false;
        }
        handler.onTdlsStatus(macAddr, status, reason);
        return true;
    }

    private static void onRingBufferData(RingBufferStatus status, byte[] buffer) {
        WifiLoggerEventHandler handler = sWifiLoggerEventHandler;
        if (handler != null) {
            handler.onRingBufferData(status, buffer);
        }
    }

    private static void onWifiAlert(byte[] buffer, int errorCode) {
        WifiLoggerEventHandler handler = sWifiLoggerEventHandler;
        if (handler != null) {
            handler.onWifiAlert(errorCode, buffer);
        }
    }

    public boolean setLoggingEventHandler(WifiLoggerEventHandler handler) {
        synchronized (sLock) {
            if (isHalStarted()) {
                int oldId = sLogCmdId;
                sLogCmdId = getNewCmdIdLocked();
                if (setLoggingEventHandlerNative(sWlan0Index, sLogCmdId)) {
                    sWifiLoggerEventHandler = handler;
                    return true;
                }
                sLogCmdId = oldId;
                return false;
            }
            return false;
        }
    }

    public boolean startLoggingRingBuffer(int verboseLevel, int flags, int maxInterval, int minDataSize, String ringName) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean startLoggingRingBufferNative = startLoggingRingBufferNative(sWlan0Index, verboseLevel, flags, maxInterval, minDataSize, ringName);
                return startLoggingRingBufferNative;
            }
            return false;
        }
    }

    public int getSupportedLoggerFeatureSet() {
        synchronized (sLock) {
            if (isHalStarted()) {
                int supportedLoggerFeatureSetNative = getSupportedLoggerFeatureSetNative(sWlan0Index);
                return supportedLoggerFeatureSetNative;
            }
            return 0;
        }
    }

    public boolean resetLogHandler() {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (sLogCmdId == -1) {
                Log.e(TAG, "Can not reset handler Before set any handler");
                return false;
            } else {
                sWifiLoggerEventHandler = null;
                if (resetLogHandlerNative(sWlan0Index, sLogCmdId)) {
                    sLogCmdId = -1;
                    return true;
                }
                return false;
            }
        }
    }

    public String getDriverVersion() {
        synchronized (sLock) {
            String driverVersionNative;
            if (isHalStarted()) {
                driverVersionNative = getDriverVersionNative(sWlan0Index);
                return driverVersionNative;
            }
            driverVersionNative = "";
            return driverVersionNative;
        }
    }

    public String getFirmwareVersion() {
        synchronized (sLock) {
            String firmwareVersionNative;
            if (isHalStarted()) {
                firmwareVersionNative = getFirmwareVersionNative(sWlan0Index);
                return firmwareVersionNative;
            }
            firmwareVersionNative = "";
            return firmwareVersionNative;
        }
    }

    public RingBufferStatus[] getRingBufferStatus() {
        synchronized (sLock) {
            if (isHalStarted()) {
                RingBufferStatus[] ringBufferStatusNative = getRingBufferStatusNative(sWlan0Index);
                return ringBufferStatusNative;
            }
            return null;
        }
    }

    public boolean getRingBufferData(String ringName) {
        synchronized (sLock) {
            if (isHalStarted()) {
                boolean ringBufferDataNative = getRingBufferDataNative(sWlan0Index, ringName);
                return ringBufferDataNative;
            }
            return false;
        }
    }

    private static void onWifiFwMemoryAvailable(byte[] buffer) {
        mFwMemoryDump = buffer;
        if (DBG) {
            Log.d(TAG, "onWifiFwMemoryAvailable is called and buffer length is: " + (buffer == null ? 0 : buffer.length));
        }
    }

    public byte[] getFwMemoryDump() {
        synchronized (sLock) {
            if (!isHalStarted()) {
                return null;
            } else if (getFwMemoryDumpNative(sWlan0Index)) {
                byte[] fwMemoryDump = mFwMemoryDump;
                mFwMemoryDump = null;
                return fwMemoryDump;
            } else {
                return null;
            }
        }
    }

    public byte[] getDriverStateDump() {
        synchronized (sLock) {
            if (isHalStarted()) {
                byte[] driverStateDumpNative = getDriverStateDumpNative(sWlan0Index);
                return driverStateDumpNative;
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startPktFateMonitoring() {
        boolean z = false;
        synchronized (sLock) {
            if (!isHalStarted()) {
                return false;
            } else if (startPktFateMonitoringNative(sWlan0Index) == 0) {
                z = true;
            }
        }
    }

    public boolean getTxPktFates(TxFateReport[] reportBufs) {
        synchronized (sLock) {
            if (isHalStarted()) {
                int res = getTxPktFatesNative(sWlan0Index, reportBufs);
                if (res != 0) {
                    Log.e(TAG, "getTxPktFatesNative returned " + res);
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    public boolean getRxPktFates(RxFateReport[] reportBufs) {
        synchronized (sLock) {
            if (isHalStarted()) {
                int res = getRxPktFatesNative(sWlan0Index, reportBufs);
                if (res != 0) {
                    Log.e(TAG, "getRxPktFatesNative returned " + res);
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    public boolean setPnoList(PnoSettings settings, PnoEventHandler eventHandler) {
        Log.e(TAG, "setPnoList cmd " + sPnoCmdId);
        synchronized (sLock) {
            if (isHalStarted()) {
                sPnoCmdId = getNewCmdIdLocked();
                sPnoEventHandler = eventHandler;
                if (setPnoListNative(sWlan0Index, sPnoCmdId, settings)) {
                    return true;
                }
            }
            sPnoEventHandler = null;
            return false;
        }
    }

    public boolean setPnoList(PnoNetwork[] list, PnoEventHandler eventHandler) {
        PnoSettings settings = new PnoSettings();
        settings.networkList = list;
        return setPnoList(settings, eventHandler);
    }

    public boolean resetPnoList() {
        Log.e(TAG, "resetPnoList cmd " + sPnoCmdId);
        synchronized (sLock) {
            if (isHalStarted()) {
                sPnoCmdId = getNewCmdIdLocked();
                sPnoEventHandler = null;
                if (resetPnoListNative(sWlan0Index, sPnoCmdId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void onPnoNetworkFound(int id, ScanResult[] results, int[] beaconCaps) {
        if (results == null) {
            Log.e(TAG, "onPnoNetworkFound null results");
            return;
        }
        Log.d(TAG, "WifiNative.onPnoNetworkFound result " + results.length);
        PnoEventHandler handler = sPnoEventHandler;
        if (sPnoCmdId == 0 || handler == null) {
            Log.d(TAG, "Ignoring Pno Network found event");
        } else {
            for (int i = 0; i < results.length; i++) {
                Log.e(TAG, "onPnoNetworkFound SSID " + results[i].SSID + " " + results[i].level + " " + results[i].frequency);
                populateScanResult(results[i], beaconCaps[i], "onPnoNetworkFound ");
                results[i].wifiSsid = WifiSsid.createFromAsciiEncoded(results[i].SSID);
            }
            handler.onPnoNetworkFound(results);
        }
    }

    public boolean setBssidBlacklist(String[] list) {
        int size = 0;
        if (list != null) {
            size = list.length;
        }
        Log.e(TAG, "setBssidBlacklist cmd " + sPnoCmdId + " size " + size);
        synchronized (sLock) {
            if (isHalStarted()) {
                sPnoCmdId = getNewCmdIdLocked();
                boolean bssidBlacklistNative = setBssidBlacklistNative(sWlan0Index, sPnoCmdId, list);
                return bssidBlacklistNative;
            }
            return false;
        }
    }

    public int startSendingOffloadedPacket(int slot, KeepalivePacketData keepAlivePacket, int period) {
        Log.d(TAG, "startSendingOffloadedPacket slot=" + slot + " period=" + period);
        String[] macAddrStr = getMacAddress().split(":");
        byte[] srcMac = new byte[6];
        for (int i = 0; i < 6; i++) {
            srcMac[i] = Integer.valueOf(Integer.parseInt(macAddrStr[i], 16)).byteValue();
        }
        synchronized (sLock) {
            if (isHalStarted()) {
                int startSendingOffloadedPacketNative = startSendingOffloadedPacketNative(sWlan0Index, slot, srcMac, keepAlivePacket.dstMac, keepAlivePacket.data, period);
                return startSendingOffloadedPacketNative;
            }
            return -1;
        }
    }

    public int stopSendingOffloadedPacket(int slot) {
        Log.d(TAG, "stopSendingOffloadedPacket " + slot);
        synchronized (sLock) {
            if (isHalStarted()) {
                int stopSendingOffloadedPacketNative = stopSendingOffloadedPacketNative(sWlan0Index, slot);
                return stopSendingOffloadedPacketNative;
            }
            return -1;
        }
    }

    private static void onRssiThresholdBreached(int id, byte curRssi) {
        WifiRssiEventHandler handler = sWifiRssiEventHandler;
        if (handler != null) {
            handler.onRssiThresholdBreached(curRssi);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0067, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int startRssiMonitoring(byte maxRssi, byte minRssi, WifiRssiEventHandler rssiEventHandler) {
        Log.d(TAG, "startRssiMonitoring: maxRssi=" + maxRssi + " minRssi=" + minRssi);
        synchronized (sLock) {
            sWifiRssiEventHandler = rssiEventHandler;
            if (isHalStarted()) {
                if (sRssiMonitorCmdId != 0) {
                    stopRssiMonitoring();
                }
                sRssiMonitorCmdId = getNewCmdIdLocked();
                Log.d(TAG, "sRssiMonitorCmdId = " + sRssiMonitorCmdId);
                int ret = startRssiMonitoringNative(sWlan0Index, sRssiMonitorCmdId, maxRssi, minRssi);
                if (ret != 0) {
                    sRssiMonitorCmdId = 0;
                }
            } else {
                return -1;
            }
        }
    }

    public int stopRssiMonitoring() {
        Log.d(TAG, "stopRssiMonitoring, cmdId " + sRssiMonitorCmdId);
        synchronized (sLock) {
            if (isHalStarted()) {
                int ret = 0;
                if (sRssiMonitorCmdId != 0) {
                    ret = stopRssiMonitoringNative(sWlan0Index, sRssiMonitorCmdId);
                }
                sRssiMonitorCmdId = 0;
                return ret;
            }
            return -1;
        }
    }

    public WifiWakeReasonAndCounts getWlanWakeReasonCount() {
        Log.d(TAG, "getWlanWakeReasonCount " + sWlan0Index);
        synchronized (sLock) {
            if (isHalStarted()) {
                WifiWakeReasonAndCounts wlanWakeReasonCountNative = getWlanWakeReasonCountNative(sWlan0Index);
                return wlanWakeReasonCountNative;
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0053, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean configureNeighborDiscoveryOffload(boolean enabled) {
        boolean z = false;
        String logMsg = "configureNeighborDiscoveryOffload(" + enabled + ")";
        Log.d(this.mTAG, logMsg);
        synchronized (sLock) {
            if (isHalStarted()) {
                int ret = configureNeighborDiscoveryOffload(sWlan0Index, enabled);
                if (ret != 0) {
                    Log.d(this.mTAG, logMsg + " returned: " + ret);
                }
                if (ret == 0) {
                    z = true;
                }
            } else {
                return false;
            }
        }
    }

    public String p2pGetVendorElems(String deviceAddress) {
        return p2pGetParam(deviceAddress, "vendor_elems");
    }

    public boolean doCtiaTestOn() {
        return doBooleanCommand("DRIVER smt-test-on");
    }

    public boolean doCtiaTestOff() {
        return doBooleanCommand("DRIVER smt-test-off");
    }

    public boolean doCtiaTestRate(int rate) {
        return doBooleanCommand("DRIVER smt-rate " + rate);
    }

    public boolean setBssExpireAge(int value) {
        return doBooleanCommand("BSS_EXPIRE_AGE " + value);
    }

    public boolean setBssExpireCount(int value) {
        return doBooleanCommand("BSS_EXPIRE_COUNT " + value);
    }

    public boolean getDisconnectFlag() {
        return this.mDisconnectCalled;
    }

    public boolean setWoWlanNormalModeCommand() {
        return doBooleanCommand("DRIVER_WOWLAN_NORMAL");
    }

    public boolean setWoWlanMagicModeCommand() {
        return doBooleanCommand("DRIVER_WOWLAN_MAGIC");
    }

    public boolean setHotspotOptimization(boolean enable) {
        if (enable) {
            return doBooleanCommand("DRIVER set_chip greenAp 1");
        }
        return doBooleanCommand("DRIVER set_chip greenAp 0");
    }

    public String getTestEnv(int channel) {
        if (channel < -1) {
            return null;
        }
        return doStringCommand("DRIVER CH_ENV_GET" + (channel == -1 ? "" : " " + channel));
    }

    public boolean setTdlsPowerSave(boolean enable) {
        if (enable) {
            return doBooleanCommand("DRIVER TDLS-PS 1");
        }
        return doBooleanCommand("DRIVER TDLS-PS 0");
    }

    public boolean setAlwaysScanState(int value) {
        return doBooleanCommand("DRIVER SETALWAYSSCANSTATE " + value);
    }
}

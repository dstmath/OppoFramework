package com.android.server.wifi.scanner;

import android.content.Context;
import android.net.wifi.WifiScanner.HotlistSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.WifiChangeSettings;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.HotlistEventHandler;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiNative.SignificantWifiChangeEventHandler;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class HalWifiScannerImpl extends WifiScannerImpl implements Callback {
    private static boolean DBG = false;
    private static final String TAG = "HalWifiScannerImpl";
    private final ChannelHelper mChannelHelper;
    private final boolean mHalBasedPnoSupported;
    private final SupplicantWifiScannerImpl mSupplicantScannerDelegate;
    private final WifiNative mWifiNative;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.HalWifiScannerImpl.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.HalWifiScannerImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.HalWifiScannerImpl.<clinit>():void");
    }

    public HalWifiScannerImpl(Context context, WifiNative wifiNative, Looper looper, Clock clock) {
        this.mWifiNative = wifiNative;
        this.mChannelHelper = new HalChannelHelper(wifiNative);
        this.mSupplicantScannerDelegate = new SupplicantWifiScannerImpl(context, wifiNative, this.mChannelHelper, looper, clock);
        this.mHalBasedPnoSupported = false;
        Log.d(TAG, "HalWifiScannerImpl is created");
    }

    public boolean handleMessage(Message msg) {
        Log.w(TAG, "Unknown message received: " + msg.what);
        return true;
    }

    public void cleanup() {
        this.mSupplicantScannerDelegate.cleanup();
    }

    public boolean getScanCapabilities(ScanCapabilities capabilities) {
        return this.mWifiNative.getScanCapabilities(capabilities);
    }

    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    public boolean startSingleScan(ScanSettings settings, ScanEventHandler eventHandler) {
        return this.mSupplicantScannerDelegate.startSingleScan(settings, eventHandler);
    }

    public ScanData getLatestSingleScanResults() {
        return this.mSupplicantScannerDelegate.getLatestSingleScanResults();
    }

    public boolean startBatchedScan(ScanSettings settings, ScanEventHandler eventHandler) {
        if (settings != null && eventHandler != null) {
            return this.mWifiNative.startScan(settings, eventHandler);
        }
        Log.w(TAG, "Invalid arguments for startBatched: settings=" + settings + ",eventHandler=" + eventHandler);
        return false;
    }

    public void stopBatchedScan() {
        this.mWifiNative.stopScan();
    }

    public void pauseBatchedScan() {
        this.mWifiNative.pauseScan();
    }

    public void restartBatchedScan() {
        this.mWifiNative.restartScan();
    }

    public ScanData[] getLatestBatchedScanResults(boolean flush) {
        return this.mWifiNative.getScanResults(flush);
    }

    public boolean setHwPnoList(PnoSettings settings, PnoEventHandler eventHandler) {
        if (this.mHalBasedPnoSupported) {
            return this.mWifiNative.setPnoList(settings, eventHandler);
        }
        return this.mSupplicantScannerDelegate.setHwPnoList(settings, eventHandler);
    }

    public boolean resetHwPnoList() {
        if (this.mHalBasedPnoSupported) {
            return this.mWifiNative.resetPnoList();
        }
        return this.mSupplicantScannerDelegate.resetHwPnoList();
    }

    public boolean isHwPnoSupported(boolean isConnectedPno) {
        if (this.mHalBasedPnoSupported) {
            return true;
        }
        return this.mSupplicantScannerDelegate.isHwPnoSupported(isConnectedPno);
    }

    public boolean shouldScheduleBackgroundScanForHwPno() {
        if (this.mHalBasedPnoSupported) {
            return true;
        }
        return this.mSupplicantScannerDelegate.shouldScheduleBackgroundScanForHwPno();
    }

    public boolean setHotlist(HotlistSettings settings, HotlistEventHandler eventHandler) {
        return this.mWifiNative.setHotlist(settings, eventHandler);
    }

    public void resetHotlist() {
        this.mWifiNative.resetHotlist();
    }

    public boolean trackSignificantWifiChange(WifiChangeSettings settings, SignificantWifiChangeEventHandler handler) {
        return this.mWifiNative.trackSignificantWifiChange(settings, handler);
    }

    public void untrackSignificantWifiChange() {
        this.mWifiNative.untrackSignificantWifiChange();
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
        if (this.mSupplicantScannerDelegate != null) {
            this.mSupplicantScannerDelegate.enableVerboseLogging(verbose);
        }
    }
}

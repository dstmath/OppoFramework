package com.android.server.wifi.scanner;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner.BssidInfo;
import android.net.wifi.WifiScanner.HotlistSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.WifiChangeSettings;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.HotlistEventHandler;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoNetwork;
import com.android.server.wifi.WifiNative.PnoSettings;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiNative.SignificantWifiChangeEventHandler;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class SupplicantWifiScannerImpl extends WifiScannerImpl implements Callback {
    private static final String ACTION_SCAN_PERIOD = "com.android.server.util.SupplicantWifiScannerImpl.action.SCAN_PERIOD";
    public static final String BACKGROUND_PERIOD_ALARM_TAG = "SupplicantWifiScannerImpl Background Scan Period";
    private static boolean DBG = false;
    private static final int MAX_APS_PER_SCAN = 32;
    public static final int MAX_HIDDEN_NETWORK_IDS_PER_SCAN = 16;
    private static final int MAX_SCAN_BUCKETS = 16;
    private static final int SCAN_BUFFER_CAPACITY = 10;
    private static final long SCAN_TIMEOUT_MS = 15000;
    private static final String TAG = "SupplicantWifiScannerImpl";
    public static final String TIMEOUT_ALARM_TAG = "SupplicantWifiScannerImpl Scan Timeout";
    private final AlarmManager mAlarmManager;
    private ScanBuffer mBackgroundScanBuffer;
    private ScanEventHandler mBackgroundScanEventHandler;
    private boolean mBackgroundScanPaused;
    private boolean mBackgroundScanPeriodPending;
    private ScanSettings mBackgroundScanSettings;
    private final ChannelHelper mChannelHelper;
    private final Clock mClock;
    private final Context mContext;
    private final Handler mEventHandler;
    private ChangeBuffer mHotlistChangeBuffer;
    private HotlistEventHandler mHotlistHandler;
    private final HwPnoDebouncer mHwPnoDebouncer;
    private final Listener mHwPnoDebouncerListener;
    private final boolean mHwPnoScanSupported;
    private LastScanSettings mLastScanSettings;
    private ScanData mLatestSingleScanResult;
    private int mNextBackgroundScanId;
    private int mNextBackgroundScanPeriod;
    private ScanEventHandler mPendingBackgroundScanEventHandler;
    private ScanSettings mPendingBackgroundScanSettings;
    private ScanEventHandler mPendingSingleScanEventHandler;
    private ScanSettings mPendingSingleScanSettings;
    private PnoEventHandler mPnoEventHandler;
    private PnoSettings mPnoSettings;
    private ScanFoolProof mScanFoolProof;
    OnAlarmListener mScanPeriodListener;
    OnAlarmListener mScanTimeoutListener;
    private Object mSettingsLock;
    private final WifiManager mWifiManager;
    private final WifiNative mWifiNative;

    private static class ChangeBuffer {
        public static int EVENT_FOUND;
        public static int EVENT_LOST;
        public static int EVENT_NONE;
        public static int STATE_FOUND;
        private int mApLostThreshold;
        private BssidInfo[] mBssidInfos;
        private boolean mFiredEvents;
        private int[] mLostCount;
        private int mMinEvents;
        private ScanResult[] mMostRecentResult;
        private int[] mPendingEvent;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.SupplicantWifiScannerImpl.ChangeBuffer.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.SupplicantWifiScannerImpl.ChangeBuffer.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.SupplicantWifiScannerImpl.ChangeBuffer.<clinit>():void");
        }

        /* synthetic */ ChangeBuffer(ChangeBuffer changeBuffer) {
            this();
        }

        private ChangeBuffer() {
            this.mBssidInfos = null;
            this.mLostCount = null;
            this.mMostRecentResult = null;
            this.mPendingEvent = null;
            this.mFiredEvents = false;
        }

        private static ScanResult findResult(List<ScanResult> results, String bssid) {
            for (int i = 0; i < results.size(); i++) {
                if (bssid.equalsIgnoreCase(((ScanResult) results.get(i)).BSSID)) {
                    return (ScanResult) results.get(i);
                }
            }
            return null;
        }

        public void setSettings(BssidInfo[] bssidInfos, int apLostThreshold, int minEvents) {
            this.mBssidInfos = bssidInfos;
            if (apLostThreshold <= 0) {
                this.mApLostThreshold = 1;
            } else {
                this.mApLostThreshold = apLostThreshold;
            }
            this.mMinEvents = minEvents;
            if (bssidInfos != null) {
                this.mLostCount = new int[bssidInfos.length];
                Arrays.fill(this.mLostCount, this.mApLostThreshold);
                this.mMostRecentResult = new ScanResult[bssidInfos.length];
                this.mPendingEvent = new int[bssidInfos.length];
                this.mFiredEvents = false;
                return;
            }
            this.mLostCount = null;
            this.mMostRecentResult = null;
            this.mPendingEvent = null;
        }

        public void clearSettings() {
            setSettings(null, 0, 0);
        }

        public ScanResult[] getLastResults(int event) {
            ArrayList<ScanResult> results = new ArrayList();
            for (int i = 0; i < this.mLostCount.length; i++) {
                if (this.mPendingEvent[i] == event) {
                    results.add(this.mMostRecentResult[i]);
                }
            }
            return (ScanResult[]) results.toArray(new ScanResult[results.size()]);
        }

        public int processScan(List<ScanResult> scanResults) {
            if (this.mBssidInfos == null) {
                return EVENT_NONE;
            }
            int i;
            if (this.mFiredEvents) {
                this.mFiredEvents = false;
                for (i = 0; i < this.mLostCount.length; i++) {
                    this.mPendingEvent[i] = EVENT_NONE;
                }
            }
            int eventCount = 0;
            int eventType = EVENT_NONE;
            for (i = 0; i < this.mLostCount.length; i++) {
                ScanResult result = findResult(scanResults, this.mBssidInfos[i].bssid);
                int rssi = Integer.MIN_VALUE;
                if (result != null) {
                    this.mMostRecentResult[i] = result;
                    rssi = result.level;
                }
                if (rssi >= this.mBssidInfos[i].low) {
                    if (this.mLostCount[i] >= this.mApLostThreshold) {
                        if (this.mPendingEvent[i] == EVENT_LOST) {
                            this.mPendingEvent[i] = EVENT_NONE;
                        } else {
                            this.mPendingEvent[i] = EVENT_FOUND;
                        }
                    }
                    this.mLostCount[i] = STATE_FOUND;
                } else if (this.mLostCount[i] < this.mApLostThreshold) {
                    int[] iArr = this.mLostCount;
                    iArr[i] = iArr[i] + 1;
                    if (this.mLostCount[i] >= this.mApLostThreshold) {
                        if (this.mPendingEvent[i] == EVENT_FOUND) {
                            this.mPendingEvent[i] = EVENT_NONE;
                        } else {
                            this.mPendingEvent[i] = EVENT_LOST;
                        }
                    }
                }
                if (SupplicantWifiScannerImpl.DBG) {
                    Log.d(SupplicantWifiScannerImpl.TAG, "ChangeBuffer BSSID: " + this.mBssidInfos[i].bssid + "=" + this.mLostCount[i] + ", " + this.mPendingEvent[i] + ", rssi=" + rssi);
                }
                if (this.mPendingEvent[i] != EVENT_NONE) {
                    eventCount++;
                    eventType |= this.mPendingEvent[i];
                }
            }
            if (SupplicantWifiScannerImpl.DBG) {
                Log.d(SupplicantWifiScannerImpl.TAG, "ChangeBuffer events count=" + eventCount + ": " + eventType);
            }
            if (eventCount < this.mMinEvents) {
                return EVENT_NONE;
            }
            this.mFiredEvents = true;
            return eventType;
        }
    }

    public static class HwPnoDebouncer {
        private static final int MINIMUM_PNO_GAP_MS = 0;
        public static final String PNO_DEBOUNCER_ALARM_TAG = "SupplicantWifiScannerImplPno Monitor";
        private final OnAlarmListener mAlarmListener;
        private final AlarmManager mAlarmManager;
        private final Clock mClock;
        private boolean mCurrentPnoState;
        private final Handler mEventHandler;
        private boolean mExpectedPnoState;
        private long mLastPnoChangeTimeStamp;
        private Listener mListener;
        private boolean mWaitForTimer;
        private final WifiNative mWifiNative;

        public interface Listener {
            void onPnoScanFailed();
        }

        public HwPnoDebouncer(WifiNative wifiNative, AlarmManager alarmManager, Handler eventHandler, Clock clock) {
            this.mLastPnoChangeTimeStamp = -1;
            this.mExpectedPnoState = false;
            this.mCurrentPnoState = false;
            this.mWaitForTimer = false;
            this.mAlarmListener = new OnAlarmListener() {
                public void onAlarm() {
                    if (SupplicantWifiScannerImpl.DBG) {
                        Log.d(SupplicantWifiScannerImpl.TAG, "PNO timer expired, expected state " + HwPnoDebouncer.this.mExpectedPnoState);
                    }
                    if (!(HwPnoDebouncer.this.updatePnoState(HwPnoDebouncer.this.mExpectedPnoState) || HwPnoDebouncer.this.mListener == null)) {
                        HwPnoDebouncer.this.mListener.onPnoScanFailed();
                    }
                    HwPnoDebouncer.this.mWaitForTimer = false;
                }
            };
            this.mWifiNative = wifiNative;
            this.mAlarmManager = alarmManager;
            this.mEventHandler = eventHandler;
            this.mClock = clock;
        }

        private boolean updatePnoState(boolean enable) {
            if (this.mCurrentPnoState == enable) {
                if (SupplicantWifiScannerImpl.DBG) {
                    Log.d(SupplicantWifiScannerImpl.TAG, "PNO state is already " + enable);
                }
                return true;
            }
            this.mLastPnoChangeTimeStamp = this.mClock.elapsedRealtime();
            if (this.mWifiNative.setPnoScan(enable)) {
                Log.d(SupplicantWifiScannerImpl.TAG, "Changed PNO state from " + this.mCurrentPnoState + " to " + enable);
                this.mCurrentPnoState = enable;
                return true;
            }
            Log.e(SupplicantWifiScannerImpl.TAG, "PNO state change to " + enable + " failed");
            this.mCurrentPnoState = false;
            return false;
        }

        private boolean setPnoState(boolean enable) {
            this.mExpectedPnoState = enable;
            if (this.mWaitForTimer) {
                return true;
            }
            long timeDifference = this.mClock.elapsedRealtime() - this.mLastPnoChangeTimeStamp;
            if (timeDifference >= 0) {
                return updatePnoState(enable);
            }
            long alarmTimeout = 0 - timeDifference;
            Log.d(SupplicantWifiScannerImpl.TAG, "Start PNO timer with delay " + alarmTimeout);
            this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + alarmTimeout, PNO_DEBOUNCER_ALARM_TAG, this.mAlarmListener, this.mEventHandler);
            this.mWaitForTimer = true;
            return true;
        }

        public boolean startPnoScan(Listener listener) {
            if (SupplicantWifiScannerImpl.DBG) {
                Log.d(SupplicantWifiScannerImpl.TAG, "Starting PNO scan");
            }
            this.mListener = listener;
            if (setPnoState(true)) {
                return true;
            }
            this.mListener = null;
            return false;
        }

        public void stopPnoScan() {
            if (SupplicantWifiScannerImpl.DBG) {
                Log.d(SupplicantWifiScannerImpl.TAG, "Stopping PNO scan");
            }
            setPnoState(false);
            this.mListener = null;
        }

        public void forceStopPnoScan() {
            if (SupplicantWifiScannerImpl.DBG) {
                Log.d(SupplicantWifiScannerImpl.TAG, "Force stopping Pno scan");
            }
            if (this.mWaitForTimer) {
                this.mAlarmManager.cancel(this.mAlarmListener);
                this.mWaitForTimer = false;
            }
            updatePnoState(false);
        }
    }

    private static class LastScanSettings {
        public boolean backgroundScanActive;
        public boolean hwPnoScanActive;
        public int maxAps;
        public PnoNetwork[] pnoNetworkList;
        public PnoEventHandler pnoScanEventHandler;
        public int reportEvents;
        public int reportNumScansThreshold;
        public int reportPercentThreshold;
        public boolean reportSingleScanFullResults;
        public int scanId;
        public boolean singleScanActive;
        public ScanEventHandler singleScanEventHandler;
        public ChannelCollection singleScanFreqs;
        public long startTime;

        public LastScanSettings(long startTime) {
            this.backgroundScanActive = false;
            this.singleScanActive = false;
            this.hwPnoScanActive = false;
            this.startTime = startTime;
        }

        public void setBackgroundScan(int scanId, int maxAps, int reportEvents, int reportNumScansThreshold, int reportPercentThreshold) {
            this.backgroundScanActive = true;
            this.scanId = scanId;
            this.maxAps = maxAps;
            this.reportEvents = reportEvents;
            this.reportNumScansThreshold = reportNumScansThreshold;
            this.reportPercentThreshold = reportPercentThreshold;
        }

        public void setSingleScan(boolean reportSingleScanFullResults, ChannelCollection singleScanFreqs, ScanEventHandler singleScanEventHandler) {
            this.singleScanActive = true;
            this.reportSingleScanFullResults = reportSingleScanFullResults;
            this.singleScanFreqs = singleScanFreqs;
            this.singleScanEventHandler = singleScanEventHandler;
        }

        public void setHwPnoScan(PnoNetwork[] pnoNetworkList, PnoEventHandler pnoScanEventHandler) {
            this.hwPnoScanActive = true;
            this.pnoNetworkList = pnoNetworkList;
            this.pnoScanEventHandler = pnoScanEventHandler;
        }
    }

    private static class ScanBuffer {
        private final ArrayDeque<ScanData> mBuffer;
        private int mCapacity;

        public ScanBuffer(int capacity) {
            this.mCapacity = capacity;
            this.mBuffer = new ArrayDeque(this.mCapacity);
        }

        public int size() {
            return this.mBuffer.size();
        }

        public int capacity() {
            return this.mCapacity;
        }

        public boolean isFull() {
            return size() == this.mCapacity;
        }

        public void add(ScanData scanData) {
            if (isFull()) {
                this.mBuffer.pollFirst();
            }
            this.mBuffer.offerLast(scanData);
        }

        public void clear() {
            this.mBuffer.clear();
        }

        public ScanData[] get() {
            return (ScanData[]) this.mBuffer.toArray(new ScanData[this.mBuffer.size()]);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.SupplicantWifiScannerImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.SupplicantWifiScannerImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.SupplicantWifiScannerImpl.<clinit>():void");
    }

    public SupplicantWifiScannerImpl(Context context, WifiNative wifiNative, ChannelHelper channelHelper, Looper looper, Clock clock) {
        this.mSettingsLock = new Object();
        this.mPendingBackgroundScanSettings = null;
        this.mPendingBackgroundScanEventHandler = null;
        this.mPendingSingleScanSettings = null;
        this.mPendingSingleScanEventHandler = null;
        this.mBackgroundScanSettings = null;
        this.mBackgroundScanEventHandler = null;
        this.mNextBackgroundScanPeriod = 0;
        this.mNextBackgroundScanId = 0;
        this.mBackgroundScanPeriodPending = false;
        this.mBackgroundScanPaused = false;
        this.mBackgroundScanBuffer = new ScanBuffer(10);
        this.mLatestSingleScanResult = new ScanData(0, 0, new ScanResult[0]);
        this.mLastScanSettings = null;
        this.mHotlistHandler = null;
        this.mHotlistChangeBuffer = new ChangeBuffer();
        this.mPnoSettings = null;
        this.mHwPnoDebouncerListener = new Listener() {
            public void onPnoScanFailed() {
                Log.e(SupplicantWifiScannerImpl.TAG, "Pno scan failure received");
                SupplicantWifiScannerImpl.this.reportPnoScanFailure();
            }
        };
        this.mScanPeriodListener = new OnAlarmListener() {
            public void onAlarm() {
                synchronized (SupplicantWifiScannerImpl.this.mSettingsLock) {
                    SupplicantWifiScannerImpl.this.handleScanPeriod();
                }
            }
        };
        this.mScanTimeoutListener = new OnAlarmListener() {
            public void onAlarm() {
                synchronized (SupplicantWifiScannerImpl.this.mSettingsLock) {
                    SupplicantWifiScannerImpl.this.handleScanTimeout();
                }
            }
        };
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mChannelHelper = channelHelper;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mEventHandler = new Handler(looper, this);
        this.mClock = clock;
        this.mHwPnoDebouncer = new HwPnoDebouncer(this.mWifiNative, this.mAlarmManager, this.mEventHandler, this.mClock);
        Log.d(TAG, "SupplicantWifiScannerImpl is created");
        this.mHwPnoScanSupported = this.mContext.getResources().getBoolean(17956888);
        WifiMonitor.getInstance().registerHandler(this.mWifiNative.getInterfaceName(), WifiMonitor.SCAN_FAILED_EVENT, this.mEventHandler);
        WifiMonitor.getInstance().registerHandler(this.mWifiNative.getInterfaceName(), WifiMonitor.SCAN_RESULTS_EVENT, this.mEventHandler);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mScanFoolProof = new ScanFoolProof(this.mEventHandler, clock, context);
    }

    public SupplicantWifiScannerImpl(Context context, WifiNative wifiNative, Looper looper, Clock clock) {
        this(context, wifiNative, new NoBandChannelHelper(), looper, clock);
    }

    public void cleanup() {
        synchronized (this.mSettingsLock) {
            this.mPendingSingleScanSettings = null;
            this.mPendingSingleScanEventHandler = null;
            stopHwPnoScan();
            stopBatchedScan();
            resetHotlist();
            untrackSignificantWifiChange();
            this.mLastScanSettings = null;
        }
    }

    public boolean getScanCapabilities(ScanCapabilities capabilities) {
        capabilities.max_scan_cache_size = Integer.MAX_VALUE;
        capabilities.max_scan_buckets = 16;
        capabilities.max_ap_cache_per_scan = 32;
        capabilities.max_rssi_sample_size = 8;
        capabilities.max_scan_reporting_threshold = 10;
        capabilities.max_hotlist_bssids = 0;
        capabilities.max_significant_wifi_change_aps = 0;
        return true;
    }

    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    public boolean startSingleScan(ScanSettings settings, ScanEventHandler eventHandler) {
        if (eventHandler == null || settings == null) {
            Log.w(TAG, "Invalid arguments for startSingleScan: settings=" + settings + ",eventHandler=" + eventHandler);
            return false;
        } else if ((this.mPendingSingleScanSettings != null || (this.mLastScanSettings != null && this.mLastScanSettings.singleScanActive)) && SupplicantState.SCANNING == this.mWifiManager.getConnectionInfo().getSupplicantState()) {
            Log.w(TAG, "A single scan is already running");
            return false;
        } else {
            synchronized (this.mSettingsLock) {
                this.mPendingSingleScanSettings = settings;
                this.mPendingSingleScanEventHandler = eventHandler;
                processPendingScans();
            }
            return true;
        }
    }

    public ScanData getLatestSingleScanResults() {
        return this.mLatestSingleScanResult;
    }

    /* JADX WARNING: Missing block: B:9:0x0035, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:14:0x0040, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:19:0x004b, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:24:0x0056, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startBatchedScan(ScanSettings settings, ScanEventHandler eventHandler) {
        if (settings == null || eventHandler == null) {
            Log.w(TAG, "Invalid arguments for startBatched: settings=" + settings + ",eventHandler=" + eventHandler);
            return false;
        } else if (settings.max_ap_per_scan < 0 || settings.max_ap_per_scan > 32 || settings.num_buckets < 0 || settings.num_buckets > 16 || settings.report_threshold_num_scans < 0 || settings.report_threshold_num_scans > 10 || settings.report_threshold_percent < 0 || settings.report_threshold_percent > 100 || settings.base_period_ms <= 0) {
            return false;
        } else {
            for (int i = 0; i < settings.num_buckets; i++) {
                if (settings.buckets[i].period_ms % settings.base_period_ms != 0) {
                    return false;
                }
            }
            synchronized (this.mSettingsLock) {
                stopBatchedScan();
                if (DBG) {
                    Log.d(TAG, "Starting scan num_buckets=" + settings.num_buckets + ", base_period=" + settings.base_period_ms + " ms");
                }
                this.mPendingBackgroundScanSettings = settings;
                this.mPendingBackgroundScanEventHandler = eventHandler;
                handleScanPeriod();
            }
            return true;
        }
    }

    public void stopBatchedScan() {
        synchronized (this.mSettingsLock) {
            if (DBG) {
                Log.d(TAG, "Stopping scan");
            }
            this.mBackgroundScanSettings = null;
            this.mBackgroundScanEventHandler = null;
            this.mPendingBackgroundScanSettings = null;
            this.mPendingBackgroundScanEventHandler = null;
            this.mBackgroundScanPaused = false;
            this.mBackgroundScanPeriodPending = false;
            unscheduleScansLocked();
        }
        processPendingScans();
    }

    public void pauseBatchedScan() {
        synchronized (this.mSettingsLock) {
            if (DBG) {
                Log.d(TAG, "Pausing scan");
            }
            if (this.mPendingBackgroundScanSettings == null) {
                this.mPendingBackgroundScanSettings = this.mBackgroundScanSettings;
                this.mPendingBackgroundScanEventHandler = this.mBackgroundScanEventHandler;
            }
            this.mBackgroundScanSettings = null;
            this.mBackgroundScanEventHandler = null;
            this.mBackgroundScanPeriodPending = false;
            this.mBackgroundScanPaused = true;
            unscheduleScansLocked();
            ScanData[] results = getLatestBatchedScanResults(true);
            if (this.mPendingBackgroundScanEventHandler != null) {
                this.mPendingBackgroundScanEventHandler.onScanPaused(results);
            }
        }
        processPendingScans();
    }

    public void restartBatchedScan() {
        synchronized (this.mSettingsLock) {
            if (DBG) {
                Log.d(TAG, "Restarting scan");
            }
            if (this.mPendingBackgroundScanEventHandler != null) {
                this.mPendingBackgroundScanEventHandler.onScanRestarted();
            }
            this.mBackgroundScanPaused = false;
            handleScanPeriod();
        }
    }

    private void unscheduleScansLocked() {
        this.mAlarmManager.cancel(this.mScanPeriodListener);
        if (this.mLastScanSettings != null) {
            this.mLastScanSettings.backgroundScanActive = false;
        }
    }

    private void handleScanPeriod() {
        synchronized (this.mSettingsLock) {
            this.mBackgroundScanPeriodPending = true;
            processPendingScans();
        }
    }

    private void handleScanTimeout() {
        Log.e(TAG, "Timed out waiting for scan result from supplicant");
        reportScanFailure();
        processPendingScans();
    }

    private boolean isDifferentPnoScanSettings(LastScanSettings newScanSettings) {
        return this.mLastScanSettings == null || !Arrays.equals(newScanSettings.pnoNetworkList, this.mLastScanSettings.pnoNetworkList);
    }

    /* JADX WARNING: Missing block: B:75:0x01d9, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPendingScans() {
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings == null || this.mLastScanSettings.hwPnoScanActive) {
                int[] hiddenNetworkIds;
                int numHiddenNetworkIds;
                int i;
                ChannelCollection allFreqs = this.mChannelHelper.createChannelCollection();
                Set<Integer> hiddenNetworkIdSet = new HashSet();
                final LastScanSettings newScanSettings = new LastScanSettings(this.mClock.elapsedRealtime());
                if (!this.mBackgroundScanPaused) {
                    if (this.mPendingBackgroundScanSettings != null) {
                        this.mBackgroundScanSettings = this.mPendingBackgroundScanSettings;
                        this.mBackgroundScanEventHandler = this.mPendingBackgroundScanEventHandler;
                        this.mNextBackgroundScanPeriod = 0;
                        this.mPendingBackgroundScanSettings = null;
                        this.mPendingBackgroundScanEventHandler = null;
                        this.mBackgroundScanPeriodPending = true;
                    }
                    if (this.mBackgroundScanPeriodPending && this.mBackgroundScanSettings != null) {
                        int reportEvents = 4;
                        for (int bucket_id = 0; bucket_id < this.mBackgroundScanSettings.num_buckets; bucket_id++) {
                            BucketSettings bucket = this.mBackgroundScanSettings.buckets[bucket_id];
                            if (this.mNextBackgroundScanPeriod % (bucket.period_ms / this.mBackgroundScanSettings.base_period_ms) == 0) {
                                if ((bucket.report_events & 1) != 0) {
                                    reportEvents |= 1;
                                }
                                if ((bucket.report_events & 2) != 0) {
                                    reportEvents |= 2;
                                }
                                if ((bucket.report_events & 4) == 0) {
                                    reportEvents &= -5;
                                }
                                allFreqs.addChannels(bucket);
                            }
                        }
                        if (!allFreqs.isEmpty()) {
                            int i2 = this.mNextBackgroundScanId;
                            this.mNextBackgroundScanId = i2 + 1;
                            newScanSettings.setBackgroundScan(i2, this.mBackgroundScanSettings.max_ap_per_scan, reportEvents, this.mBackgroundScanSettings.report_threshold_num_scans, this.mBackgroundScanSettings.report_threshold_percent);
                        }
                        hiddenNetworkIds = this.mBackgroundScanSettings.hiddenNetworkIds;
                        if (hiddenNetworkIds != null) {
                            numHiddenNetworkIds = Math.min(hiddenNetworkIds.length, 16);
                            for (i = 0; i < numHiddenNetworkIds; i++) {
                                hiddenNetworkIdSet.add(Integer.valueOf(hiddenNetworkIds[i]));
                            }
                        }
                        this.mNextBackgroundScanPeriod++;
                        this.mBackgroundScanPeriodPending = false;
                        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + ((long) this.mBackgroundScanSettings.base_period_ms), BACKGROUND_PERIOD_ALARM_TAG, this.mScanPeriodListener, this.mEventHandler);
                    }
                }
                if (this.mPendingSingleScanSettings != null) {
                    boolean reportFullResults = false;
                    ChannelCollection singleScanFreqs = this.mChannelHelper.createChannelCollection();
                    for (i = 0; i < this.mPendingSingleScanSettings.num_buckets; i++) {
                        BucketSettings bucketSettings = this.mPendingSingleScanSettings.buckets[i];
                        if ((bucketSettings.report_events & 2) != 0) {
                            reportFullResults = true;
                        }
                        singleScanFreqs.addChannels(bucketSettings);
                        allFreqs.addChannels(bucketSettings);
                    }
                    newScanSettings.setSingleScan(reportFullResults, singleScanFreqs, this.mPendingSingleScanEventHandler);
                    hiddenNetworkIds = this.mPendingSingleScanSettings.hiddenNetworkIds;
                    if (hiddenNetworkIds != null) {
                        numHiddenNetworkIds = Math.min(hiddenNetworkIds.length, 16);
                        for (i = 0; i < numHiddenNetworkIds; i++) {
                            hiddenNetworkIdSet.add(Integer.valueOf(hiddenNetworkIds[i]));
                        }
                    }
                    this.mPendingSingleScanSettings = null;
                    this.mPendingSingleScanEventHandler = null;
                }
                if ((newScanSettings.backgroundScanActive || newScanSettings.singleScanActive) && !allFreqs.isEmpty()) {
                    pauseHwPnoScan();
                    Set<Integer> freqs = allFreqs.getSupplicantScanFreqs();
                    if (this.mWifiNative.scan(freqs, hiddenNetworkIdSet)) {
                        if (DBG) {
                            Log.d(TAG, "Starting wifi scan for freqs=" + freqs + ", background=" + newScanSettings.backgroundScanActive + ", single=" + newScanSettings.singleScanActive);
                        }
                        this.mScanFoolProof.scanCmdSetSuccess();
                        this.mLastScanSettings = newScanSettings;
                        this.mAlarmManager.set(2, this.mClock.elapsedRealtime() + SCAN_TIMEOUT_MS, TIMEOUT_ALARM_TAG, this.mScanTimeoutListener, this.mEventHandler);
                    } else {
                        Log.e(TAG, "Failed to start scan, freqs=" + freqs);
                        SupplicantState supplicantState = SupplicantState.DISCONNECTED;
                        WifiInfo wifiInfo = null;
                        if (this.mWifiManager != null) {
                            wifiInfo = this.mWifiManager.getConnectionInfo();
                        }
                        if (wifiInfo != null) {
                            supplicantState = wifiInfo.getSupplicantState();
                        }
                        if (!SupplicantState.isHandshakeState(supplicantState)) {
                            this.mScanFoolProof.scanCmdRejcet();
                        }
                        this.mEventHandler.post(new Runnable() {
                            public void run() {
                                if (newScanSettings.singleScanEventHandler != null) {
                                    newScanSettings.singleScanEventHandler.onScanStatus(3);
                                }
                            }
                        });
                    }
                } else if (isHwPnoScanRequired()) {
                    boolean status;
                    newScanSettings.setHwPnoScan(this.mPnoSettings.networkList, this.mPnoEventHandler);
                    if (isDifferentPnoScanSettings(newScanSettings)) {
                        status = startHwPnoScan();
                    } else {
                        status = startHwPnoScan();
                    }
                    if (status) {
                        this.mLastScanSettings = newScanSettings;
                    } else {
                        Log.e(TAG, "Failed to start PNO scan");
                        this.mEventHandler.post(new Runnable() {
                            public void run() {
                                if (SupplicantWifiScannerImpl.this.mPnoEventHandler != null) {
                                    SupplicantWifiScannerImpl.this.mPnoEventHandler.onPnoScanFailed();
                                }
                                SupplicantWifiScannerImpl.this.mPnoSettings = null;
                                SupplicantWifiScannerImpl.this.mPnoEventHandler = null;
                            }
                        });
                    }
                }
            }
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                this.mAlarmManager.cancel(this.mScanTimeoutListener);
                this.mScanFoolProof.scanResultGot();
                pollLatestScanData();
                processPendingScans();
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                Log.w(TAG, "Scan failed");
                this.mAlarmManager.cancel(this.mScanTimeoutListener);
                this.mScanFoolProof.scanResultFail();
                reportScanFailure();
                processPendingScans();
                break;
        }
        return true;
    }

    private void reportScanFailure() {
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings != null) {
                if (this.mLastScanSettings.singleScanEventHandler != null) {
                    this.mLastScanSettings.singleScanEventHandler.onScanStatus(3);
                }
                this.mLastScanSettings = null;
            }
        }
    }

    private void reportPnoScanFailure() {
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings != null && this.mLastScanSettings.hwPnoScanActive) {
                if (this.mLastScanSettings.pnoScanEventHandler != null) {
                    this.mLastScanSettings.pnoScanEventHandler.onPnoScanFailed();
                }
                this.mPnoSettings = null;
                this.mPnoEventHandler = null;
                this.mLastScanSettings = null;
            }
        }
    }

    private void pollLatestScanData() {
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings == null) {
                return;
            }
            ArrayList<ScanDetail> nativeResults;
            int i;
            if (DBG) {
                Log.d(TAG, "Polling scan data for scan: " + this.mLastScanSettings.scanId);
            }
            if (this.mWifiManager.getWifiState() == 0) {
                Log.e(TAG, "Wifi in turning OFF state, dont get scanresults");
                nativeResults = new ArrayList();
            } else {
                nativeResults = this.mWifiNative.getScanResults();
            }
            List<ScanResult> singleScanResults = new ArrayList();
            List<ScanResult> backgroundScanResults = new ArrayList();
            List<ScanResult> hwPnoScanResults = new ArrayList();
            for (i = 0; i < nativeResults.size(); i++) {
                ScanResult result = ((ScanDetail) nativeResults.get(i)).getScanResult();
                if (result.timestamp / 1000 > this.mLastScanSettings.startTime) {
                    if (this.mLastScanSettings.backgroundScanActive) {
                        backgroundScanResults.add(result);
                    }
                    if (this.mLastScanSettings.singleScanActive && this.mLastScanSettings.singleScanFreqs.containsChannel(result.frequency)) {
                        singleScanResults.add(result);
                    } else {
                        Log.d(TAG, "pollLatestScanData filtered by freq :  BSSID = " + result.BSSID + " req= " + result.frequency);
                    }
                    if (this.mLastScanSettings.hwPnoScanActive) {
                        hwPnoScanResults.add(result);
                    }
                }
            }
            if (this.mLastScanSettings.backgroundScanActive) {
                if (!(this.mBackgroundScanEventHandler == null || (this.mLastScanSettings.reportEvents & 2) == 0)) {
                    for (ScanResult scanResult : backgroundScanResults) {
                        this.mBackgroundScanEventHandler.onFullScanResult(scanResult, 0);
                    }
                }
                Collections.sort(backgroundScanResults, SCAN_RESULT_SORT_COMPARATOR);
                ScanResult[] scanResultsArray = new ScanResult[Math.min(this.mLastScanSettings.maxAps, backgroundScanResults.size())];
                for (i = 0; i < scanResultsArray.length; i++) {
                    scanResultsArray[i] = (ScanResult) backgroundScanResults.get(i);
                }
                if ((this.mLastScanSettings.reportEvents & 4) == 0) {
                    this.mBackgroundScanBuffer.add(new ScanData(this.mLastScanSettings.scanId, 0, scanResultsArray));
                }
                if (!(this.mBackgroundScanEventHandler == null || ((this.mLastScanSettings.reportEvents & 2) == 0 && (this.mLastScanSettings.reportEvents & 1) == 0 && (this.mLastScanSettings.reportEvents != 0 || (this.mBackgroundScanBuffer.size() < (this.mBackgroundScanBuffer.capacity() * this.mLastScanSettings.reportPercentThreshold) / 100 && this.mBackgroundScanBuffer.size() < this.mLastScanSettings.reportNumScansThreshold))))) {
                    this.mBackgroundScanEventHandler.onScanStatus(0);
                }
                if (this.mHotlistHandler != null) {
                    int event = this.mHotlistChangeBuffer.processScan(backgroundScanResults);
                    if ((ChangeBuffer.EVENT_FOUND & event) != 0) {
                        this.mHotlistHandler.onHotlistApFound(this.mHotlistChangeBuffer.getLastResults(ChangeBuffer.EVENT_FOUND));
                    }
                    if ((ChangeBuffer.EVENT_LOST & event) != 0) {
                        this.mHotlistHandler.onHotlistApLost(this.mHotlistChangeBuffer.getLastResults(ChangeBuffer.EVENT_LOST));
                    }
                }
            }
            if (this.mLastScanSettings.singleScanActive && this.mLastScanSettings.singleScanEventHandler != null) {
                if (this.mLastScanSettings.reportSingleScanFullResults) {
                    for (ScanResult scanResult2 : singleScanResults) {
                        this.mLastScanSettings.singleScanEventHandler.onFullScanResult(scanResult2, 0);
                    }
                }
                Collections.sort(singleScanResults, SCAN_RESULT_SORT_COMPARATOR);
                this.mLatestSingleScanResult = new ScanData(this.mLastScanSettings.scanId, 0, 0, this.mLastScanSettings.singleScanFreqs.isAllChannels(), (ScanResult[]) singleScanResults.toArray(new ScanResult[singleScanResults.size()]));
                this.mLastScanSettings.singleScanEventHandler.onScanStatus(0);
            }
            if (this.mLastScanSettings.hwPnoScanActive && this.mLastScanSettings.pnoScanEventHandler != null) {
                ScanResult[] pnoScanResultsArray = new ScanResult[hwPnoScanResults.size()];
                for (i = 0; i < pnoScanResultsArray.length; i++) {
                    pnoScanResultsArray[i] = (ScanResult) hwPnoScanResults.get(i);
                }
                this.mLastScanSettings.pnoScanEventHandler.onPnoNetworkFound(pnoScanResultsArray);
            }
            this.mLastScanSettings = null;
        }
    }

    public ScanData[] getLatestBatchedScanResults(boolean flush) {
        ScanData[] results;
        synchronized (this.mSettingsLock) {
            results = this.mBackgroundScanBuffer.get();
            if (flush) {
                this.mBackgroundScanBuffer.clear();
            }
        }
        return results;
    }

    private boolean setNetworkPriorities(PnoNetwork[] networkList) {
        if (networkList != null) {
            if (DBG) {
                Log.i(TAG, "Enable network and Set priorities for PNO.");
            }
            int length = networkList.length;
            int i = 0;
            while (i < length) {
                PnoNetwork network = networkList[i];
                if (this.mWifiNative.setNetworkVariable(network.networkId, "priority", Integer.toString(network.priority))) {
                    i++;
                } else {
                    Log.e(TAG, "Set priority failed for: " + network.networkId);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean startHwPnoScan() {
        return this.mHwPnoDebouncer.startPnoScan(this.mHwPnoDebouncerListener);
    }

    private void stopHwPnoScan() {
        this.mHwPnoDebouncer.stopPnoScan();
    }

    private void pauseHwPnoScan() {
        this.mHwPnoDebouncer.forceStopPnoScan();
    }

    private boolean restartHwPnoScan() {
        this.mHwPnoDebouncer.forceStopPnoScan();
        return this.mHwPnoDebouncer.startPnoScan(this.mHwPnoDebouncerListener);
    }

    private boolean isHwPnoScanRequired(boolean isConnectedPno) {
        return (isConnectedPno ? 0 : 1) & this.mHwPnoScanSupported;
    }

    private boolean isHwPnoScanRequired() {
        if (this.mPnoSettings == null) {
            return false;
        }
        return isHwPnoScanRequired(this.mPnoSettings.isConnected);
    }

    public boolean setHwPnoList(PnoSettings settings, PnoEventHandler eventHandler) {
        synchronized (this.mSettingsLock) {
            if (this.mPnoSettings != null) {
                Log.w(TAG, "Already running a PNO scan");
                return false;
            }
            this.mPnoEventHandler = eventHandler;
            this.mPnoSettings = settings;
            if (setNetworkPriorities(settings.networkList)) {
                processPendingScans();
                return true;
            }
            return false;
        }
    }

    public boolean resetHwPnoList() {
        synchronized (this.mSettingsLock) {
            if (this.mPnoSettings == null) {
                Log.w(TAG, "No PNO scan running");
                return false;
            }
            this.mPnoEventHandler = null;
            this.mPnoSettings = null;
            stopHwPnoScan();
            return true;
        }
    }

    public boolean isHwPnoSupported(boolean isConnectedPno) {
        return isHwPnoScanRequired(isConnectedPno);
    }

    public boolean shouldScheduleBackgroundScanForHwPno() {
        return false;
    }

    public boolean setHotlist(HotlistSettings settings, HotlistEventHandler eventHandler) {
        if (settings == null || eventHandler == null) {
            return false;
        }
        synchronized (this.mSettingsLock) {
            this.mHotlistHandler = eventHandler;
            this.mHotlistChangeBuffer.setSettings(settings.bssidInfos, settings.apLostThreshold, 1);
        }
        return true;
    }

    public void resetHotlist() {
        synchronized (this.mSettingsLock) {
            this.mHotlistChangeBuffer.clearSettings();
            this.mHotlistHandler = null;
        }
    }

    public boolean trackSignificantWifiChange(WifiChangeSettings settings, SignificantWifiChangeEventHandler handler) {
        return false;
    }

    public void untrackSignificantWifiChange() {
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
        if (this.mScanFoolProof != null) {
            this.mScanFoolProof.enableVerboseLogging(verbose);
        }
    }
}

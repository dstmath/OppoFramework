package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest.Builder;
import android.net.metrics.DnsEvent;
import android.net.metrics.IDnsEventListener.Stub;
import android.net.metrics.IpConnectivityLog;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

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
public class DnsEventListenerService extends Stub {
    private static final boolean DBG = true;
    private static final int MAX_LOOKUPS_PER_DNS_EVENT = 100;
    public static final String SERVICE_NAME = "dns_listener";
    private static final String TAG = null;
    private static final boolean VDBG = false;
    private final ConnectivityManager mCm;
    @GuardedBy("this")
    private final SortedMap<Integer, DnsEventBatch> mEventBatches;
    private final IpConnectivityLog mMetricsLog;
    private final NetworkCallback mNetworkCallback;

    private class DnsEventBatch {
        private int mEventCount;
        private final byte[] mEventTypes = new byte[100];
        private final int[] mLatenciesMs = new int[100];
        private final int mNetId;
        private final byte[] mReturnCodes = new byte[100];

        public DnsEventBatch(int netId) {
            this.mNetId = netId;
        }

        public void addResult(byte eventType, byte returnCode, int latencyMs) {
            this.mEventTypes[this.mEventCount] = eventType;
            this.mReturnCodes[this.mEventCount] = returnCode;
            this.mLatenciesMs[this.mEventCount] = latencyMs;
            this.mEventCount++;
            if (this.mEventCount == 100) {
                logAndClear();
            }
        }

        public void logAndClear() {
            if (this.mEventCount != 0) {
                DnsEventListenerService.this.mMetricsLog.log(new DnsEvent(this.mNetId, Arrays.copyOf(this.mEventTypes, this.mEventCount), Arrays.copyOf(this.mReturnCodes, this.mEventCount), Arrays.copyOf(this.mLatenciesMs, this.mEventCount)));
                Object[] objArr = new Object[2];
                objArr[0] = Integer.valueOf(this.mEventCount);
                objArr[1] = Integer.valueOf(this.mNetId);
                DnsEventListenerService.maybeLog(String.format("Logging %d results for netId %d", objArr));
                this.mEventCount = 0;
            }
        }

        public String toString() {
            Object[] objArr = new Object[3];
            objArr[0] = getClass().getSimpleName();
            objArr[1] = Integer.valueOf(this.mNetId);
            objArr[2] = Integer.valueOf(this.mEventCount);
            return String.format("%s %d %d", objArr);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.DnsEventListenerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.DnsEventListenerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.DnsEventListenerService.<clinit>():void");
    }

    public DnsEventListenerService(Context context) {
        this((ConnectivityManager) context.getSystemService(ConnectivityManager.class), new IpConnectivityLog());
    }

    public DnsEventListenerService(ConnectivityManager cm, IpConnectivityLog log) {
        this.mEventBatches = new TreeMap();
        this.mNetworkCallback = new NetworkCallback() {
            public void onLost(Network network) {
                synchronized (DnsEventListenerService.this) {
                    DnsEventBatch batch = (DnsEventBatch) DnsEventListenerService.this.mEventBatches.remove(Integer.valueOf(network.netId));
                    if (batch != null) {
                        batch.logAndClear();
                    }
                }
            }
        };
        this.mCm = cm;
        this.mMetricsLog = log;
        this.mCm.registerNetworkCallback(new Builder().clearCapabilities().build(), this.mNetworkCallback);
    }

    public synchronized void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs) {
        Object[] objArr = new Object[4];
        objArr[0] = Integer.valueOf(netId);
        objArr[1] = Integer.valueOf(eventType);
        objArr[2] = Integer.valueOf(returnCode);
        objArr[3] = Integer.valueOf(latencyMs);
        maybeVerboseLog(String.format("onDnsEvent(%d, %d, %d, %d)", objArr));
        DnsEventBatch batch = (DnsEventBatch) this.mEventBatches.get(Integer.valueOf(netId));
        if (batch == null) {
            batch = new DnsEventBatch(netId);
            this.mEventBatches.put(Integer.valueOf(netId), batch);
        }
        batch.addResult((byte) eventType, (byte) returnCode, latencyMs);
    }

    public synchronized void dump(PrintWriter writer) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(TAG + ":");
        pw.increaseIndent();
        for (DnsEventBatch batch : this.mEventBatches.values()) {
            pw.println(batch.toString());
        }
        pw.decreaseIndent();
    }

    private static void maybeLog(String s) {
        Log.d(TAG, s);
    }

    private static void maybeVerboseLog(String s) {
    }
}

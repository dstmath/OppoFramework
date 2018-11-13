package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityMetricsEvent;
import android.net.IIpConnectivityMetrics.Stub;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.SystemService;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.IpConnectivityEvent;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
public final class IpConnectivityMetrics extends SystemService {
    private static final boolean DBG = false;
    private static final int DEFAULT_BUFFER_SIZE = 2000;
    private static final String SERVICE_NAME = "connmetrics";
    private static final String TAG = null;
    public final Impl impl;
    @GuardedBy("mLock")
    private ArrayList<ConnectivityMetricsEvent> mBuffer;
    @GuardedBy("mLock")
    private int mCapacity;
    private DnsEventListenerService mDnsListener;
    @GuardedBy("mLock")
    private int mDropped;
    private final Object mLock;

    public final class Impl extends Stub {
        static final String CMD_DEFAULT = "stats";
        static final String CMD_FLUSH = "flush";
        static final String CMD_LIST = "list";
        static final String CMD_STATS = "stats";

        public int logEvent(ConnectivityMetricsEvent event) {
            enforceConnectivityInternalPermission();
            return IpConnectivityMetrics.this.append(event);
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            enforceDumpPermission();
            String cmd = args.length > 0 ? args[0] : "stats";
            if (cmd.equals(CMD_FLUSH)) {
                IpConnectivityMetrics.this.cmdFlush(fd, pw, args);
            } else if (cmd.equals(CMD_LIST)) {
                IpConnectivityMetrics.this.cmdList(fd, pw, args);
            } else if (cmd.equals("stats")) {
                IpConnectivityMetrics.this.cmdStats(fd, pw, args);
            } else {
                IpConnectivityMetrics.this.cmdDefault(fd, pw, args);
            }
        }

        private void enforceConnectivityInternalPermission() {
            enforcePermission("android.permission.CONNECTIVITY_INTERNAL");
        }

        private void enforceDumpPermission() {
            enforcePermission("android.permission.DUMP");
        }

        private void enforcePermission(String what) {
            IpConnectivityMetrics.this.getContext().enforceCallingOrSelfPermission(what, "IpConnectivityMetrics");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.IpConnectivityMetrics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.IpConnectivityMetrics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.IpConnectivityMetrics.<clinit>():void");
    }

    public IpConnectivityMetrics(Context ctx) {
        super(ctx);
        this.mLock = new Object();
        this.impl = new Impl();
        initBuffer();
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mDnsListener = new DnsEventListenerService(getContext());
            publishBinderService(SERVICE_NAME, this.impl);
            publishBinderService(DnsEventListenerService.SERVICE_NAME, this.mDnsListener);
        }
    }

    public int bufferCapacity() {
        return DEFAULT_BUFFER_SIZE;
    }

    private void initBuffer() {
        synchronized (this.mLock) {
            this.mDropped = 0;
            this.mCapacity = bufferCapacity();
            this.mBuffer = new ArrayList(this.mCapacity);
        }
    }

    private int append(ConnectivityMetricsEvent event) {
        synchronized (this.mLock) {
            int left = this.mCapacity - this.mBuffer.size();
            if (event == null) {
                return left;
            } else if (left == 0) {
                this.mDropped++;
                return 0;
            } else {
                this.mBuffer.add(event);
                int i = left - 1;
                return i;
            }
        }
    }

    private String flushEncodedOutput() {
        ArrayList<ConnectivityMetricsEvent> events;
        int dropped;
        synchronized (this.mLock) {
            events = this.mBuffer;
            dropped = this.mDropped;
            initBuffer();
        }
        try {
            return Base64.encodeToString(IpConnectivityEventBuilder.serialize(dropped, events), 0);
        } catch (IOException e) {
            Log.e(TAG, "could not serialize events", e);
            return IElsaManager.EMPTY_PACKAGE;
        }
    }

    private void cmdFlush(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print(flushEncodedOutput());
    }

    private void cmdList(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            List<ConnectivityMetricsEvent> events = new ArrayList(this.mBuffer);
        }
        if (args.length <= 1 || !args[1].equals("proto")) {
            for (ConnectivityMetricsEvent ev : events) {
                pw.println(ev.toString());
            }
            return;
        }
        for (IpConnectivityEvent ev2 : IpConnectivityEventBuilder.toProto((List) events)) {
            pw.print(ev2.toString());
        }
    }

    private void cmdStats(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            pw.println("Buffered events: " + this.mBuffer.size());
            pw.println("Buffer capacity: " + this.mCapacity);
            pw.println("Dropped events: " + this.mDropped);
        }
        if (this.mDnsListener != null) {
            this.mDnsListener.dump(pw);
        }
    }

    private void cmdDefault(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length == 0) {
            pw.println("No command");
        } else {
            pw.println("Unknown command " + TextUtils.join(" ", args));
        }
    }
}

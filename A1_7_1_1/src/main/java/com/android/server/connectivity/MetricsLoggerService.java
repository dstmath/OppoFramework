package com.android.server.connectivity;

import android.app.PendingIntent;
import android.content.Context;
import android.net.ConnectivityMetricsEvent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.net.IConnectivityMetricsLogger.Stub;
import android.os.Binder;
import android.os.Parcel;
import android.text.format.DateUtils;
import android.util.Log;
import com.android.server.SystemService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

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
public class MetricsLoggerService extends SystemService {
    private static final boolean DBG = true;
    private static String TAG;
    private static final boolean VDBG = false;
    private final int EVENTS_NOTIFICATION_THRESHOLD;
    private final int MAX_NUMBER_OF_EVENTS;
    private final int THROTTLING_MAX_NUMBER_OF_MESSAGES_PER_COMPONENT;
    private final long THROTTLING_TIME_INTERVAL_MILLIS;
    final MetricsLoggerImpl mBinder;
    private int mEventCounter;
    private final ArrayDeque<ConnectivityMetricsEvent> mEvents;
    private long mLastEventReference;
    private final int[] mThrottlingCounters;
    private long mThrottlingIntervalBoundaryMillis;

    final class MetricsLoggerImpl extends Stub {
        private final ArrayList<PendingIntent> mPendingIntents = new ArrayList();

        MetricsLoggerImpl() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (MetricsLoggerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump ConnectivityMetricsLoggerService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            boolean dumpSerializedSize = false;
            boolean dumpEvents = false;
            boolean dumpDebugInfo = false;
            for (String arg : args) {
                if (arg.equals("--debug")) {
                    dumpDebugInfo = true;
                } else if (arg.equals("--events")) {
                    dumpEvents = true;
                } else if (arg.equals("--size")) {
                    dumpSerializedSize = true;
                } else if (arg.equals("--all")) {
                    dumpDebugInfo = true;
                    dumpEvents = true;
                    dumpSerializedSize = true;
                }
            }
            synchronized (MetricsLoggerService.this.mEvents) {
                pw.println("Number of events: " + MetricsLoggerService.this.mEvents.size());
                pw.println("Counter: " + MetricsLoggerService.this.mEventCounter);
                if (MetricsLoggerService.this.mEvents.size() > 0) {
                    pw.println("Time span: " + DateUtils.formatElapsedTime((System.currentTimeMillis() - ((ConnectivityMetricsEvent) MetricsLoggerService.this.mEvents.peekFirst()).timestamp) / 1000));
                }
                if (dumpSerializedSize) {
                    Parcel p = Parcel.obtain();
                    for (ConnectivityMetricsEvent e : MetricsLoggerService.this.mEvents) {
                        p.writeParcelable(e, 0);
                    }
                    pw.println("Serialized data size: " + p.dataSize());
                    p.recycle();
                }
                if (dumpEvents) {
                    pw.println();
                    pw.println("Events:");
                    for (ConnectivityMetricsEvent e2 : MetricsLoggerService.this.mEvents) {
                        pw.println(e2.toString());
                    }
                }
            }
            if (dumpDebugInfo) {
                synchronized (MetricsLoggerService.this.mThrottlingCounters) {
                    pw.println();
                    for (int i = 0; i < 5; i++) {
                        if (MetricsLoggerService.this.mThrottlingCounters[i] > 0) {
                            pw.println("Throttling Counter #" + i + ": " + MetricsLoggerService.this.mThrottlingCounters[i]);
                        }
                    }
                    pw.println("Throttling Time Remaining: " + DateUtils.formatElapsedTime((MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis - System.currentTimeMillis()) / 1000));
                }
            }
            synchronized (this.mPendingIntents) {
                if (!this.mPendingIntents.isEmpty()) {
                    pw.println();
                    pw.println("Pending intents:");
                    for (PendingIntent pi : this.mPendingIntents) {
                        pw.println(pi.toString());
                    }
                }
            }
        }

        public long logEvent(ConnectivityMetricsEvent event) {
            ConnectivityMetricsEvent[] events = new ConnectivityMetricsEvent[1];
            events[0] = event;
            return logEvents(events);
        }

        /* JADX WARNING: Missing block: B:24:0x00ba, code:
            r14 = false;
            r4 = com.android.server.connectivity.MetricsLoggerService.-get2(r18.this$0);
     */
        /* JADX WARNING: Missing block: B:25:0x00c3, code:
            monitor-enter(r4);
     */
        /* JADX WARNING: Missing block: B:26:0x00c4, code:
            r3 = 0;
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r5 = r19.length;
     */
        /* JADX WARNING: Missing block: B:29:0x00c8, code:
            if (r3 >= r5) goto L_0x00fe;
     */
        /* JADX WARNING: Missing block: B:30:0x00ca, code:
            r12 = r19[r3];
     */
        /* JADX WARNING: Missing block: B:31:0x00ce, code:
            if (r12.componentTag == r8) goto L_0x00f4;
     */
        /* JADX WARNING: Missing block: B:32:0x00d0, code:
            android.util.Log.wtf(com.android.server.connectivity.MetricsLoggerService.-get0(), "Unexpected tag: " + r12.componentTag);
     */
        /* JADX WARNING: Missing block: B:34:0x00ef, code:
            monitor-exit(r4);
     */
        /* JADX WARNING: Missing block: B:35:0x00f0, code:
            return -1;
     */
        /* JADX WARNING: Missing block: B:40:?, code:
            com.android.server.connectivity.MetricsLoggerService.-wrap0(r18.this$0, r12);
            r3 = r3 + 1;
     */
        /* JADX WARNING: Missing block: B:41:0x00fe, code:
            r3 = r18.this$0;
            com.android.server.connectivity.MetricsLoggerService.-set1(r3, com.android.server.connectivity.MetricsLoggerService.-get3(r3) + ((long) r19.length));
            r3 = r18.this$0;
            com.android.server.connectivity.MetricsLoggerService.-set0(r3, com.android.server.connectivity.MetricsLoggerService.-get1(r3) + r19.length);
     */
        /* JADX WARNING: Missing block: B:42:0x012a, code:
            if (com.android.server.connectivity.MetricsLoggerService.-get1(r18.this$0) < 300) goto L_0x0135;
     */
        /* JADX WARNING: Missing block: B:43:0x012c, code:
            com.android.server.connectivity.MetricsLoggerService.-set0(r18.this$0, 0);
     */
        /* JADX WARNING: Missing block: B:44:0x0134, code:
            r14 = true;
     */
        /* JADX WARNING: Missing block: B:45:0x0135, code:
            monitor-exit(r4);
     */
        /* JADX WARNING: Missing block: B:46:0x0136, code:
            if (r14 == false) goto L_0x018c;
     */
        /* JADX WARNING: Missing block: B:47:0x0138, code:
            r15 = r18.mPendingIntents;
     */
        /* JADX WARNING: Missing block: B:48:0x013c, code:
            monitor-enter(r15);
     */
        /* JADX WARNING: Missing block: B:50:?, code:
            r13 = r18.mPendingIntents.iterator();
     */
        /* JADX WARNING: Missing block: B:52:0x0149, code:
            if (r13.hasNext() == false) goto L_0x018b;
     */
        /* JADX WARNING: Missing block: B:53:0x014b, code:
            r2 = (android.app.PendingIntent) r13.next();
     */
        /* JADX WARNING: Missing block: B:55:?, code:
            r2.send(r18.this$0.getContext(), 0, null, null, null);
     */
        /* JADX WARNING: Missing block: B:59:?, code:
            android.util.Log.e(com.android.server.connectivity.MetricsLoggerService.-get0(), "Pending intent canceled: " + r2);
            r18.mPendingIntents.remove(r2);
     */
        /* JADX WARNING: Missing block: B:67:0x018b, code:
            monitor-exit(r15);
     */
        /* JADX WARNING: Missing block: B:69:0x018e, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long logEvents(ConnectivityMetricsEvent[] events) {
            MetricsLoggerService.this.enforceConnectivityInternalPermission();
            if (events == null || events.length == 0) {
                Log.wtf(MetricsLoggerService.TAG, "No events passed to logEvents()");
                return -1;
            }
            int componentTag = events[0].componentTag;
            if (componentTag < 0 || componentTag >= 5) {
                Log.wtf(MetricsLoggerService.TAG, "Unexpected tag: " + componentTag);
                return -1;
            }
            synchronized (MetricsLoggerService.this.mThrottlingCounters) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis > MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis) {
                    MetricsLoggerService.this.resetThrottlingCounters(currentTimeMillis);
                }
                int[] -get4 = MetricsLoggerService.this.mThrottlingCounters;
                -get4[componentTag] = -get4[componentTag] + events.length;
                if (MetricsLoggerService.this.mThrottlingCounters[componentTag] > 1000) {
                    Log.w(MetricsLoggerService.TAG, "Too many events from #" + componentTag + ". Block until " + MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis);
                    long -get5 = MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis;
                    return -get5;
                }
            }
        }

        public ConnectivityMetricsEvent[] getEvents(Reference reference) {
            MetricsLoggerService.this.enforceDumpPermission();
            long ref = reference.getValue();
            synchronized (MetricsLoggerService.this.mEvents) {
                if (ref > MetricsLoggerService.this.mLastEventReference) {
                    Log.e(MetricsLoggerService.TAG, "Invalid reference");
                    reference.setValue(MetricsLoggerService.this.mLastEventReference);
                    return null;
                }
                if (ref < MetricsLoggerService.this.mLastEventReference - ((long) MetricsLoggerService.this.mEvents.size())) {
                    ref = MetricsLoggerService.this.mLastEventReference - ((long) MetricsLoggerService.this.mEvents.size());
                }
                int numEventsToSkip = MetricsLoggerService.this.mEvents.size() - ((int) (MetricsLoggerService.this.mLastEventReference - ref));
                ConnectivityMetricsEvent[] result = new ConnectivityMetricsEvent[(MetricsLoggerService.this.mEvents.size() - numEventsToSkip)];
                int i = 0;
                Iterator e$iterator = MetricsLoggerService.this.mEvents.iterator();
                while (true) {
                    int i2 = i;
                    if (e$iterator.hasNext()) {
                        ConnectivityMetricsEvent e = (ConnectivityMetricsEvent) e$iterator.next();
                        if (numEventsToSkip > 0) {
                            numEventsToSkip--;
                            i = i2;
                        } else {
                            i = i2 + 1;
                            result[i2] = e;
                        }
                    } else {
                        reference.setValue(MetricsLoggerService.this.mLastEventReference);
                        return result;
                    }
                }
            }
        }

        public boolean register(PendingIntent newEventsIntent) {
            MetricsLoggerService.this.enforceDumpPermission();
            synchronized (this.mPendingIntents) {
                if (this.mPendingIntents.remove(newEventsIntent)) {
                    Log.w(MetricsLoggerService.TAG, "Replacing registered pending intent");
                }
                this.mPendingIntents.add(newEventsIntent);
            }
            return true;
        }

        public void unregister(PendingIntent newEventsIntent) {
            MetricsLoggerService.this.enforceDumpPermission();
            synchronized (this.mPendingIntents) {
                if (!this.mPendingIntents.remove(newEventsIntent)) {
                    Log.e(MetricsLoggerService.TAG, "Pending intent is not registered");
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.MetricsLoggerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.MetricsLoggerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.MetricsLoggerService.<clinit>():void");
    }

    public MetricsLoggerService(Context context) {
        super(context);
        this.EVENTS_NOTIFICATION_THRESHOLD = 300;
        this.MAX_NUMBER_OF_EVENTS = 1000;
        this.THROTTLING_MAX_NUMBER_OF_MESSAGES_PER_COMPONENT = 1000;
        this.THROTTLING_TIME_INTERVAL_MILLIS = 3600000;
        this.mEventCounter = 0;
        this.mLastEventReference = 0;
        this.mThrottlingCounters = new int[5];
        this.mEvents = new ArrayDeque();
        this.mBinder = new MetricsLoggerImpl();
    }

    public void onStart() {
        resetThrottlingCounters(System.currentTimeMillis());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            publishBinderService("connectivity_metrics_logger", this.mBinder);
        }
    }

    private void enforceConnectivityInternalPermission() {
        getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "MetricsLoggerService");
    }

    private void enforceDumpPermission() {
        getContext().enforceCallingOrSelfPermission("android.permission.DUMP", "MetricsLoggerService");
    }

    private void resetThrottlingCounters(long currentTimeMillis) {
        synchronized (this.mThrottlingCounters) {
            for (int i = 0; i < this.mThrottlingCounters.length; i++) {
                this.mThrottlingCounters[i] = 0;
            }
            this.mThrottlingIntervalBoundaryMillis = 3600000 + currentTimeMillis;
        }
    }

    private void addEvent(ConnectivityMetricsEvent e) {
        while (this.mEvents.size() >= 1000) {
            this.mEvents.removeFirst();
        }
        this.mEvents.addLast(e);
    }
}

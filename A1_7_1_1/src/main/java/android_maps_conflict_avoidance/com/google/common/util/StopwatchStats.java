package android_maps_conflict_avoidance.com.google.common.util;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import java.util.Hashtable;

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
public class StopwatchStats {
    private static Hashtable instancesByName;
    private Clock clock;
    private int count;
    private final short eventType;
    private int last;
    private String logStatus;
    private int max;
    private int min;
    private String name;
    private long start;
    private long total;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.util.StopwatchStats.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.util.StopwatchStats.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.util.StopwatchStats.<clinit>():void");
    }

    public StopwatchStats(Clock clock, String name, String logStatus, short eventType) {
        this.min = Integer.MAX_VALUE;
        this.start = -1;
        this.clock = clock;
        this.name = name;
        this.logStatus = logStatus;
        this.eventType = eventType;
        instancesByName.put(name, this);
    }

    public StopwatchStats(String name, String logStatus, short eventType) {
        this(Config.getInstance().getClock(), name, logStatus, eventType);
    }

    public void start() {
        this.start = getCurrentTime();
    }

    public void stop() {
        if (this.start != -1) {
            addSample((int) (getCurrentTime() - this.start));
            this.start = -1;
        }
    }

    private void addSample(int msec) {
        this.last = msec;
        this.total += (long) msec;
        this.count++;
        if (this.min > msec) {
            this.min = msec;
        }
        if (this.max < msec) {
            this.max = msec;
        }
        if (this.logStatus != null && this.eventType != (short) -1) {
            Log.addEvent(this.eventType, this.logStatus, "" + msec);
        }
    }

    public int getAverage() {
        return this.count > 0 ? (int) (((this.total + ((long) this.count)) - 1) / ((long) this.count)) : 0;
    }

    public int getMin() {
        return this.count > 0 ? this.min : 0;
    }

    public int getMax() {
        return this.max;
    }

    public int getLast() {
        return this.last;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this.name != null) {
            sb.append(this.name);
            sb.append(":");
        }
        sb.append(getMin());
        sb.append(",");
        sb.append(getAverage());
        sb.append(",");
        sb.append(getMax());
        sb.append(":");
        sb.append(getLast());
        sb.append(":");
        sb.append(this.total);
        return sb.toString();
    }

    private long getCurrentTime() {
        return this.clock.relativeTimeMillis();
    }
}

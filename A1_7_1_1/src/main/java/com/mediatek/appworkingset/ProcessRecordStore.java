package com.mediatek.appworkingset;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.mediatek.am.IAWSProcessRecord;

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
class ProcessRecordStore implements IAWSProcessRecord {
    static final boolean DEBUG = false;
    static final int SAMPLE_REFRESH_TIME = 30000;
    private static int SERVICE_ADJ = 0;
    private static int SERVICE_B_ADJ = 0;
    static final String TAG = "AWSPRStore";
    int adj;
    boolean killed;
    boolean killedByAm;
    int lastAdj;
    long lastSampleTime;
    long lastSampledMemory;
    ArrayMap<String, Long> launchingMemory;
    String packageName;
    int packageVer;
    int pid;
    ArrayMap<String, ProcessStateHolder> pkgList;
    String processName;
    int procstats;
    int uid;
    String waitingToKill;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.appworkingset.ProcessRecordStore.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.appworkingset.ProcessRecordStore.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.appworkingset.ProcessRecordStore.<clinit>():void");
    }

    ProcessRecordStore(IAWSProcessRecord iAWSProcessRecord) {
        this.pkgList = new ArrayMap();
        this.launchingMemory = new ArrayMap();
        this.lastSampledMemory = 0;
        update(iAWSProcessRecord);
        this.lastSampledMemory = 0;
    }

    ProcessRecordStore(String str, int i) {
        this.pkgList = new ArrayMap();
        this.launchingMemory = new ArrayMap();
        this.lastSampledMemory = 0;
        this.processName = str;
        this.uid = i;
    }

    ProcessRecordStore(String str, int i, int i2) {
        this.pkgList = new ArrayMap();
        this.launchingMemory = new ArrayMap();
        this.lastSampledMemory = 0;
        this.processName = str;
        this.pid = i2;
        this.uid = i;
    }

    ProcessRecordStore(String str, int i, String str2, long j) {
        this.pkgList = new ArrayMap();
        this.launchingMemory = new ArrayMap();
        this.lastSampledMemory = 0;
        this.processName = str;
        this.uid = i;
        updateLaunchMem(str2, j);
    }

    public void update(IAWSProcessRecord iAWSProcessRecord) {
        this.processName = iAWSProcessRecord.getProcName();
        this.packageName = iAWSProcessRecord.getPkgName();
        this.packageVer = iAWSProcessRecord.getPkgVer();
        this.pid = iAWSProcessRecord.getPid();
        this.uid = iAWSProcessRecord.getUid();
        this.pkgList = iAWSProcessRecord.getpkgList();
        this.adj = iAWSProcessRecord.getAdj();
        this.procstats = iAWSProcessRecord.getprocState();
        this.killedByAm = iAWSProcessRecord.isKilledByAm();
        this.killed = iAWSProcessRecord.isKilled();
        this.waitingToKill = iAWSProcessRecord.getWaitingToKill();
    }

    public String getProcName() {
        return this.processName;
    }

    public String getPkgName() {
        return this.packageName;
    }

    public int getPkgVer() {
        return this.packageVer;
    }

    public int getPid() {
        return this.pid;
    }

    public int getUid() {
        return this.uid;
    }

    public ArrayMap<String, ProcessStateHolder> getpkgList() {
        return this.pkgList;
    }

    public int getAdj() {
        return this.adj;
    }

    public int getprocState() {
        return this.procstats;
    }

    public boolean isKilled() {
        return this.killed;
    }

    public boolean isKilledByAm() {
        return this.killedByAm;
    }

    public String getWaitingToKill() {
        return this.waitingToKill;
    }

    public void setPid(int i) {
        this.pid = i;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public void setAdj(int i) {
        this.adj = i;
    }

    public void setprocState(int i) {
        this.procstats = i;
    }

    public void updateLaunchMem(String str, long j) {
        this.launchingMemory.put(str, Long.valueOf((getLaunchMem(str) + j) / 2));
    }

    public void updateSampledMem(long j) {
        this.lastSampledMemory = j;
        this.lastSampleTime = SystemClock.uptimeMillis();
        this.lastAdj = this.adj;
    }

    public long getLaunchMem(String str) {
        if (this.launchingMemory.containsKey(str)) {
            return ((Long) this.launchingMemory.get(str)).longValue();
        }
        return 0;
    }

    public long getSampledMem() {
        if (this.adj == SERVICE_ADJ && this.lastAdj == SERVICE_B_ADJ) {
            return this.lastSampledMemory;
        }
        if (this.adj == SERVICE_B_ADJ && this.lastAdj == SERVICE_ADJ) {
            return this.lastSampledMemory;
        }
        long uptimeMillis = SystemClock.uptimeMillis() - this.lastSampleTime;
        if (this.adj == this.lastAdj) {
            if ((uptimeMillis <= 30000 ? 1 : null) != null) {
                return this.lastSampledMemory;
            }
        }
        return 0;
    }

    protected void dump() {
        Log.v(TAG, "Dump ProcessRecordStore:+ " + this.processName + "(" + this.pid + ":" + this.uid + ")launchMem:" + this.launchingMemory + "sampleMem:" + this.lastSampledMemory);
    }
}

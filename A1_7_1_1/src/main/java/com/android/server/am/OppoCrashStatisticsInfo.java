package com.android.server.am;

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
public class OppoCrashStatisticsInfo {
    public static boolean DEBUG = false;
    public static final String TAG = "OppoCrashStatisticsInfo";
    protected long mCurStartTime;
    protected long mFirstCrashTime;
    protected String mPkgName;
    protected String mProcessName;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoCrashStatisticsInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoCrashStatisticsInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoCrashStatisticsInfo.<clinit>():void");
    }

    public OppoCrashStatisticsInfo(String processName, String pkgName, long firstTime) {
        this.mProcessName = processName;
        this.mPkgName = pkgName;
        this.mFirstCrashTime = firstTime;
    }

    public OppoCrashStatisticsInfo(String processName, long firstTime) {
        this.mProcessName = processName;
        this.mFirstCrashTime = firstTime;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public long getFirstStartTime() {
        return this.mFirstCrashTime;
    }

    public void setFirstStartTime(long firstStartTime) {
        this.mFirstCrashTime = firstStartTime;
    }

    public long getCurStartTime() {
        return this.mCurStartTime;
    }

    public void setCurStartTime(long curStartTime) {
        this.mCurStartTime = curStartTime;
    }

    public String toString() {
        return "OppoCrashStatisticsInfo { mProcessName=" + this.mProcessName + " ,mFirstCrashTime=" + this.mFirstCrashTime;
    }
}

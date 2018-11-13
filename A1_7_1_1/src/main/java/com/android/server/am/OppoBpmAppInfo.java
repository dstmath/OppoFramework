package com.android.server.am;

import com.android.server.oppo.IElsaManager;
import java.util.HashMap;
import java.util.Map.Entry;

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
public class OppoBpmAppInfo {
    static final String STATE_INIT = "init";
    static final String STATE_KILLED = "killed";
    static final String STATE_RESUME = "resume";
    static final String STATE_SUSPEND = "suspend";
    static final String STATE_UNSUSPEND = "unsuspend";
    public static final String TAG = "OppoProcessManager";
    private static OppoBpmAppInfo mOppoBpmAppMonitorInfo;
    private long mLifeTime;
    private long mLifeTmpTime;
    String mProcessName;
    private String mProcessState;
    private String mRealSuspendPercent;
    HashMap<Integer, Integer> mResumeInfo;
    private final Object mResumeLock;
    private long mResumeTime;
    int mSuspendCount;
    private String mSuspendPercent;
    private long mSuspendTime;
    HashMap<Integer, Integer> mUnSuspendInfo;
    private final Object mUnSuspendLock;
    private long mUnSuspendTime;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoBpmAppInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoBpmAppInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoBpmAppInfo.<clinit>():void");
    }

    protected OppoBpmAppInfo() {
        this.mSuspendCount = 0;
        this.mProcessName = "unknow";
        this.mUnSuspendInfo = new HashMap();
        this.mResumeInfo = new HashMap();
        this.mUnSuspendLock = new Object();
        this.mResumeLock = new Object();
        this.mSuspendTime = 0;
        this.mUnSuspendTime = 0;
        this.mResumeTime = 0;
        this.mLifeTime = 0;
        this.mLifeTmpTime = 0;
        this.mSuspendPercent = "0";
        this.mRealSuspendPercent = "0";
        this.mProcessState = STATE_INIT;
    }

    static OppoBpmAppInfo builder(String processName) {
        OppoBpmAppInfo bpmAppInfo = new OppoBpmAppInfo();
        bpmAppInfo.setProcessName(processName);
        return bpmAppInfo;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public String getSuspendCount() {
        return Integer.toString(this.mSuspendCount);
    }

    public int getSuspendCountNum() {
        return this.mSuspendCount;
    }

    public String getSuspendTime() {
        return Long.toString(this.mSuspendTime);
    }

    public long getSuspendTimeLong() {
        return this.mSuspendTime;
    }

    public void increaseSuspendCount() {
        this.mSuspendCount++;
    }

    public void increaseSuspendTime(long intervalTime) {
        this.mSuspendTime += intervalTime;
    }

    public long getUnSuspendTimeLong() {
        return this.mUnSuspendTime;
    }

    public void increaseUnSuspendTime(long intervalTime) {
        this.mUnSuspendTime += intervalTime;
    }

    protected void increaseResumeTime(long intervalTime) {
        this.mResumeTime += intervalTime;
    }

    public long getLifeTimeLong() {
        return this.mLifeTime;
    }

    protected void setBeginLifeTime(long beginTime) {
        this.mLifeTmpTime = beginTime;
    }

    protected void setEndLifeTime(long endTime) {
        if (this.mLifeTmpTime > 0) {
            this.mLifeTime += endTime - this.mLifeTmpTime;
            this.mLifeTmpTime = 0;
        }
    }

    protected void setCurrentProcessState(String currentState) {
        this.mProcessState = currentState;
    }

    protected String getCurrentProcessState() {
        return this.mProcessState;
    }

    protected void resetTime() {
        this.mSuspendTime = 0;
        this.mUnSuspendTime = 0;
        this.mResumeTime = 0;
        this.mLifeTime = 0;
        this.mLifeTmpTime = 0;
        this.mSuspendPercent = "0";
        this.mRealSuspendPercent = "0";
    }

    public void increaseUnSuspendInfo(Integer reason) {
        synchronized (this.mUnSuspendLock) {
            if (this.mUnSuspendInfo.containsKey(reason)) {
                this.mUnSuspendInfo.put(reason, Integer.valueOf(((Integer) this.mUnSuspendInfo.get(reason)).intValue() + 1));
            } else {
                this.mUnSuspendInfo.put(reason, Integer.valueOf(1));
            }
        }
    }

    public void increaseResumeInfo(Integer reason) {
        synchronized (this.mResumeLock) {
            if (this.mResumeInfo.containsKey(reason)) {
                this.mResumeInfo.put(reason, Integer.valueOf(((Integer) this.mResumeInfo.get(reason)).intValue() + 1));
            } else {
                this.mResumeInfo.put(reason, Integer.valueOf(1));
            }
        }
    }

    public boolean isResumeNumZero() {
        boolean result;
        synchronized (this.mResumeLock) {
            result = this.mResumeInfo.isEmpty();
        }
        return result;
    }

    public void setSuspendPercent(String suspendPercent) {
        this.mSuspendPercent = suspendPercent;
    }

    public void setRealSuspendPercent(String suspendPercent) {
        this.mRealSuspendPercent = suspendPercent;
    }

    public void clear() {
        this.mUnSuspendInfo.clear();
        this.mResumeInfo.clear();
    }

    public HashMap<String, String> getBpmAppMap() {
        HashMap<String, String> appMap = new HashMap();
        appMap.put("proc", getProcessName());
        appMap.put("s_num", getSuspendCount());
        appMap.put("s_time", getSuspendTime());
        synchronized (this.mUnSuspendLock) {
            for (Entry<Integer, Integer> entry : this.mUnSuspendInfo.entrySet()) {
                appMap.put("us_" + Integer.toString(((Integer) entry.getKey()).intValue()), Integer.toString(((Integer) entry.getValue()).intValue()));
            }
        }
        synchronized (this.mResumeLock) {
            for (Entry<Integer, Integer> entry2 : this.mResumeInfo.entrySet()) {
                appMap.put("r_" + Integer.toString(((Integer) entry2.getKey()).intValue()), Integer.toString(((Integer) entry2.getValue()).intValue()));
            }
        }
        return appMap;
    }

    public String formatToString() {
        Integer value;
        String str = IElsaManager.EMPTY_PACKAGE;
        StringBuilder sb = new StringBuilder();
        sb.append(getProcessName()).append(" S_count: ").append(this.mSuspendCount).append(" S_time: ").append(this.mSuspendTime).append(" US_time: ").append(this.mUnSuspendTime).append(" R_time: ").append(this.mResumeTime).append(" LT: ").append(this.mLifeTime).append(" US: ");
        synchronized (this.mUnSuspendLock) {
            for (Entry<Integer, Integer> entry : this.mUnSuspendInfo.entrySet()) {
                value = (Integer) entry.getValue();
                sb.append((Integer) entry.getKey()).append(":").append(value).append(" ");
            }
        }
        sb.append(" R: ");
        synchronized (this.mResumeLock) {
            for (Entry<Integer, Integer> entry2 : this.mResumeInfo.entrySet()) {
                value = (Integer) entry2.getValue();
                sb.append((Integer) entry2.getKey()).append(":").append(value).append(" ");
            }
        }
        sb.append(" PER: ").append(this.mSuspendPercent);
        sb.append(" REAL_PER: ").append(this.mRealSuspendPercent);
        return sb.toString();
    }

    public String toString() {
        return formatToString();
    }
}

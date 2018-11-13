package com.android.server.am;

import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
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
public class OppoCrashStatisticsManager {
    private static final long CRASH_TIMEOUT = 300000;
    public static boolean DEBUG = false;
    public static final String TAG = "OppoCrashDataStatistics";
    private static OppoCrashStatisticsManager mOppoCrashManager;
    private ActivityManagerService mActivityManager;
    private ActivityManagerService mAms;
    List<OppoCrashStatisticsInfo> mCrashAppList;
    private final Object mLock;
    private ProcessRecord mProcessRecord;
    private String mTag;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoCrashStatisticsManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoCrashStatisticsManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoCrashStatisticsManager.<clinit>():void");
    }

    public OppoCrashStatisticsManager() {
        this.mActivityManager = null;
        this.mCrashAppList = new ArrayList();
        this.mAms = null;
        this.mLock = new Object();
    }

    public static OppoCrashStatisticsManager getInstance() {
        if (mOppoCrashManager == null) {
            mOppoCrashManager = new OppoCrashStatisticsManager();
        }
        return mOppoCrashManager;
    }

    public void setActivityManager(ActivityManagerService ams) {
        this.mAms = ams;
    }

    public boolean collectCrashAppInfo() {
        if (this.mProcessRecord == null) {
            return false;
        }
        String processName = this.mProcessRecord.processName;
        if (processName == null) {
            return false;
        }
        synchronized (this.mLock) {
            OppoCrashStatisticsInfo appCrashInfo = getCrashAppInfoInList(processName);
            if (DEBUG) {
                Slog.v(TAG, "collectCrashAppInfo appCrashInfo==" + appCrashInfo);
            }
            if (appCrashInfo == null) {
                OppoCrashStatisticsInfo info = new OppoCrashStatisticsInfo(processName, System.currentTimeMillis());
                this.mCrashAppList.add(info);
                Slog.v(TAG, "collectCrashAppInfo add==" + info);
                return true;
            } else if (System.currentTimeMillis() > appCrashInfo.getFirstStartTime() + 300000) {
                appCrashInfo.setFirstStartTime(System.currentTimeMillis());
                return true;
            } else {
                return false;
            }
        }
    }

    public OppoCrashStatisticsInfo getCrashAppInfoInList(String processName) {
        OppoCrashStatisticsInfo resultInfo = null;
        Iterator it = this.mCrashAppList.iterator();
        while (it.hasNext()) {
            OppoCrashStatisticsInfo appinfo = (OppoCrashStatisticsInfo) it.next();
            String proName = appinfo.getProcessName();
            if (proName != null && proName.equals(processName)) {
                resultInfo = appinfo;
            } else if (System.currentTimeMillis() > appinfo.getFirstStartTime() + 300000) {
                it.remove();
            }
        }
        return resultInfo;
    }

    public void setProcessRecord(ProcessRecord processRecord) {
        this.mProcessRecord = processRecord;
    }

    public String getProcessName() {
        if (this.mProcessRecord == null) {
            return null;
        }
        return this.mProcessRecord.processName;
    }
}

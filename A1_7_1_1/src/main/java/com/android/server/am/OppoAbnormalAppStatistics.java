package com.android.server.am;

import android.os.Handler;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

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
public class OppoAbnormalAppStatistics {
    private static final String TAG = null;
    static final String TYPE_ACTIVITY = "activity";
    static final int TYPE_ACTIVITY_PASS = 2;
    static final int TYPE_ACTIVITY_RESTRICT = 1;
    static final String TYPE_BROADCAST = "broadcast";
    static final int TYPE_BROADCAST_PASS = 4;
    static final int TYPE_BROADCAST_RESTRICT = 3;
    static final int TYPE_OTHER_PASS = 12;
    static final int TYPE_OTHER_RESTRICT = 11;
    static final String TYPE_PROVIDER = "content provider";
    static final int TYPE_PROVIDER_PASS = 8;
    static final int TYPE_PROVIDER_RESTRICT = 7;
    static final String TYPE_REPEAT = "repeat";
    static final int TYPE_REPEAT_PASS = 21;
    static final int TYPE_REPEAT_RESTRICT = 20;
    static final String TYPE_RESTART = "restart";
    static final int TYPE_RESTART_PASS = 10;
    static final int TYPE_RESTART_RESTRICT = 9;
    static final String TYPE_SERVICE = "service";
    static final int TYPE_SERVICE_PASS = 6;
    static final int TYPE_SERVICE_RESTRICT = 5;
    static final String UPLOAD_AAR_KEY_A_PASS_NUM = "a_pnum";
    static final String UPLOAD_AAR_KEY_A_RESTRICT_NUM = "a_rnum";
    static final String UPLOAD_AAR_KEY_B_PASS_NUM = "b_pnum";
    static final String UPLOAD_AAR_KEY_B_RESTRICT_NUM = "b_rnum";
    static final String UPLOAD_AAR_KEY_OTHER_PASS_NUM = "other_pnum";
    static final String UPLOAD_AAR_KEY_OTHER_RESTRICT_NUM = "other_rnum";
    static final String UPLOAD_AAR_KEY_PKGNAME = "pkg";
    static final String UPLOAD_AAR_KEY_P_PASS_NUM = "p_pnum";
    static final String UPLOAD_AAR_KEY_P_RESTRICT_NUM = "p_rnum";
    static final String UPLOAD_AAR_KEY_REPEAT_PASS_NUM = "repeat_pnum";
    static final String UPLOAD_AAR_KEY_REPEAT_RESTRICT_NUM = "repeat_rnum";
    static final String UPLOAD_AAR_KEY_R_PASS_NUM = "r_pnum";
    static final String UPLOAD_AAR_KEY_R_RESTRICT_NUM = "r_rnum";
    static final String UPLOAD_AAR_KEY_S_PASS_NUM = "s_pnum";
    static final String UPLOAD_AAR_KEY_S_RESTRICT_NUM = "s_rnum";
    static final String UPLOAD_ABNORMAL_APP_RESTRICT_EVENTID = "abnormalApp_restrict";
    static final int UPLOAD_COUNT_NUM = 200;
    static final String UPLOAD_LOGTAG = "20089";
    private static OppoAbnormalAppStatistics mOppoAbnormalAppStatistics;
    private OppoAbnormalAppManager mAbnormalAppManager;
    private ActivityManagerService mAms;
    private List<OppoAbnormalAppInfo> mCollectAbnormalAppInfoList;
    private Handler mHandler;
    private final Object mListLock;
    private boolean mSwitchMonitor;

    private class CollectAbnormalAppRunnable implements Runnable {
        private String mHostType;
        private boolean mIsRestrict;
        private String mPkgName;

        public CollectAbnormalAppRunnable(String pkgName, String hostType, boolean isRestrict) {
            this.mIsRestrict = isRestrict;
            this.mPkgName = pkgName;
            this.mHostType = hostType;
        }

        public void run() {
            int length;
            OppoAbnormalAppStatistics.this.collectAbnormalAppInfo(this.mIsRestrict, this.mPkgName, this.mHostType);
            synchronized (OppoAbnormalAppStatistics.this.mListLock) {
                length = OppoAbnormalAppStatistics.this.mCollectAbnormalAppInfoList.size();
            }
            if (length >= OppoAppStartupManagerUtils.getInstance().getCheckCount()) {
                OppoAbnormalAppStatistics.this.uploadAbnormalAppInfoList();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAbnormalAppStatistics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAbnormalAppStatistics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAbnormalAppStatistics.<clinit>():void");
    }

    private OppoAbnormalAppStatistics() {
        this.mListLock = new Object();
        this.mSwitchMonitor = false;
        this.mAbnormalAppManager = null;
        this.mAms = null;
        this.mHandler = null;
        this.mCollectAbnormalAppInfoList = new ArrayList();
    }

    public static final OppoAbnormalAppStatistics getInstance() {
        if (mOppoAbnormalAppStatistics == null) {
            mOppoAbnormalAppStatistics = new OppoAbnormalAppStatistics();
        }
        return mOppoAbnormalAppStatistics;
    }

    protected void init(OppoAbnormalAppManager abnormalAppManager, Handler handler, ActivityManagerService ams) {
        this.mAbnormalAppManager = abnormalAppManager;
        this.mHandler = handler;
        this.mAms = ams;
        this.mSwitchMonitor = OppoAppStartupManagerUtils.getInstance().isForumVersion();
    }

    protected void collectAbnormalAppInfo(String pkgName, String hostType, boolean isRestrict) {
        if (isNeedUpload() && this.mHandler != null && this.mAbnormalAppManager != null) {
            if (this.mAbnormalAppManager.DynamicDebug) {
                Slog.d(TAG, "collectAppStartInfo: pkgName = " + pkgName + " hostType = " + hostType + " isRestrict = " + isRestrict);
            }
            this.mHandler.post(new CollectAbnormalAppRunnable(pkgName, hostType, isRestrict));
        }
    }

    private void collectAbnormalAppInfo(boolean isRestrict, String pkgName, String hostType) {
        int type;
        if (TYPE_ACTIVITY.equals(hostType)) {
            if (isRestrict) {
                type = 1;
            } else {
                type = 2;
            }
        } else if ("broadcast".equals(hostType)) {
            if (isRestrict) {
                type = 3;
            } else {
                type = 4;
            }
        } else if ("service".equals(hostType)) {
            if (isRestrict) {
                type = 5;
            } else {
                type = 6;
            }
        } else if (TYPE_PROVIDER.equals(hostType)) {
            if (isRestrict) {
                type = 7;
            } else {
                type = 8;
            }
        } else if (TYPE_RESTART.equals(hostType)) {
            if (isRestrict) {
                type = 9;
            } else {
                type = 10;
            }
        } else if (TYPE_REPEAT.equals(hostType)) {
            if (isRestrict) {
                type = 20;
            } else {
                type = 21;
            }
        } else if (isRestrict) {
            type = 11;
        } else {
            type = 12;
        }
        OppoAbnormalAppInfo appInfo = getAbnormalAppInfo(pkgName);
        if (appInfo == null) {
            OppoAbnormalAppInfo info = OppoAbnormalAppInfo.builder(isRestrict, pkgName, type);
            synchronized (this.mListLock) {
                this.mCollectAbnormalAppInfoList.add(info);
            }
            return;
        }
        appInfo.increaseTypeCount(isRestrict, type);
    }

    private OppoAbnormalAppInfo getAbnormalAppInfo(String pkgName) {
        synchronized (this.mListLock) {
            for (OppoAbnormalAppInfo appInfo : this.mCollectAbnormalAppInfoList) {
                if (appInfo.getPkgName().equals(pkgName)) {
                    return appInfo;
                }
            }
            return null;
        }
    }

    protected void uploadAbnormalAppInfoList() {
        if (!this.mCollectAbnormalAppInfoList.isEmpty() && this.mAbnormalAppManager != null) {
            List<Map<String, String>> uploadList = new ArrayList();
            synchronized (this.mListLock) {
                int length = this.mCollectAbnormalAppInfoList.size();
                for (int i = 0; i < length; i++) {
                    OppoAbnormalAppInfo appInfo = (OppoAbnormalAppInfo) this.mCollectAbnormalAppInfoList.get(i);
                    if (appInfo != null) {
                        uploadList.add(appInfo.getAbnormalAppMap());
                    }
                }
                this.mCollectAbnormalAppInfoList.clear();
            }
            if (this.mAbnormalAppManager.DynamicDebug) {
                Slog.d(TAG, "AbnormalAppList size " + uploadList.size());
                for (Map<String, String> info : uploadList) {
                    Slog.d(TAG, "AbnormalAppList info " + info);
                }
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, UPLOAD_LOGTAG, UPLOAD_ABNORMAL_APP_RESTRICT_EVENTID, uploadList, false);
            }
        }
    }

    public boolean isNeedUpload() {
        return this.mSwitchMonitor;
    }
}

package com.android.server.net;

import android.util.Slog;
import android.util.TrustedTime;

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
public class OppoRefreshTimeTask {
    private static final boolean DEBUG = false;
    private static final int MAX_COUNT_TO_WAIT = 4;
    private static final long MIN_FORCE_UPDATE_TIME_INTERVAL = 12000;
    private static final String TAG = "NetworkStatsService.OppoRefreshTimeTask";
    private static final int TIME_TO_WAIT_PER_TIME_IN_MILLS = 100;
    private static OppoRefreshTimeTask mInstall;
    private long mLastSysCurTimeForceUpdate;
    private RefreshTimeThread mRefreshTimeThread;
    private final TrustedTime mTime;

    private class RefreshTimeThread extends Thread {
        private boolean mIsWaiting = false;
        private boolean mRefreshRes = false;
        private boolean mRunning = true;

        public void exitRunning() {
            synchronized (this) {
                this.mRunning = false;
                notify();
            }
        }

        public void notifyToRefreshTime() {
            if (this.mIsWaiting) {
                synchronized (this) {
                    this.mRefreshRes = false;
                    notify();
                }
            }
        }

        public boolean getRefreshRes() {
            return this.mRefreshRes;
        }

        public void run() {
            while (this.mRunning) {
                synchronized (this) {
                    try {
                        this.mIsWaiting = true;
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                this.mIsWaiting = false;
                if (OppoRefreshTimeTask.this.mTime != null) {
                    OppoRefreshTimeTask.this.mTime.forceRefresh();
                }
                this.mRefreshRes = true;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.net.OppoRefreshTimeTask.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.net.OppoRefreshTimeTask.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.OppoRefreshTimeTask.<clinit>():void");
    }

    public OppoRefreshTimeTask(TrustedTime time) {
        this.mRefreshTimeThread = null;
        this.mLastSysCurTimeForceUpdate = 0;
        this.mTime = time;
        this.mRefreshTimeThread = new RefreshTimeThread();
        this.mRefreshTimeThread.start();
    }

    public static OppoRefreshTimeTask getInstall(TrustedTime time) {
        if (mInstall == null) {
            mInstall = new OppoRefreshTimeTask(time);
        }
        return mInstall;
    }

    public void triggerRefreshTime() {
        this.mRefreshTimeThread.notifyToRefreshTime();
        int queryTimeCount = 0;
        while (!this.mRefreshTimeThread.getRefreshRes() && queryTimeCount < 4) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Slog.w(TAG, "sleep failed!", e);
            }
            queryTimeCount++;
        }
    }

    public boolean forceUpdateFreqControl() {
        boolean allowUpdate = true;
        long curTime = System.currentTimeMillis();
        if (0 == this.mLastSysCurTimeForceUpdate) {
            this.mLastSysCurTimeForceUpdate = curTime;
            return true;
        }
        if (curTime - this.mLastSysCurTimeForceUpdate < MIN_FORCE_UPDATE_TIME_INTERVAL) {
            allowUpdate = false;
        }
        if (allowUpdate) {
            this.mLastSysCurTimeForceUpdate = curTime;
        }
        return allowUpdate;
    }
}

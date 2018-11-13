package com.android.server.power;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.WorkSource;
import java.io.PrintWriter;
import java.util.ArrayList;

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
class OppoWakeLockCheck {
    private static final boolean ADBG = false;
    public static final String ATAG = "OppoWakeLockCheck";
    private static final int MSG_PARTIAL_WAKELOCK_ACQUIRE = 3;
    private static final int MSG_PARTIAL_WAKELOCK_CHECK = 1;
    private static final int MSG_PARTIAL_WAKELOCK_RELEASE = 4;
    public static final int MSG_PARTIAL_WAKELOCK_TIMEOUT = 7;
    public static final int MSG_POSSIBLE_PLAYER = 9;
    private static final int MSG_SCREEN_OFF = 5;
    private static final int MSG_SCREEN_ON = 6;
    private static final int MSG_SCREEN_ON_WAKELOCK_CHECK = 2;
    private static final long SCREEN_ON_WAKELOCK_CHECK_DELAY = 30000;
    private final CommonUtil mCommonUtil;
    private WorkerHandler mHandler;
    private final OppoPartialWakeLockCheck mPartialWakeLock;
    private final OppoScreenOnWakeLockCheck mScreenOnWakeLock;
    private final SuspendBlocker mSuspendBlocker;
    private boolean msgPartialWakelockSent;
    private boolean msgScreenOnWakelockSent;

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.wakeLockHeldContinuouslyCheck(1);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 2:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mScreenOnWakeLock.check();
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 3:
                    OppoWakeLockCheck.this.mPartialWakeLock.noteStartWakeLock(msg.getData());
                    return;
                case 4:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.noteStopWakeLock(msg.getData());
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 5:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.onScreenOff(1);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 6:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.onScreenOn(1);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 7:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.wakeLockTimeout(msg);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                case 9:
                    OppoWakeLockCheck.this.mSuspendBlocker.acquire();
                    OppoWakeLockCheck.this.mPartialWakeLock.possiblePlayer(msg);
                    OppoWakeLockCheck.this.mSuspendBlocker.release();
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.power.OppoWakeLockCheck.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.power.OppoWakeLockCheck.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.OppoWakeLockCheck.<clinit>():void");
    }

    public OppoWakeLockCheck(ArrayList<WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, SuspendBlocker suspendBlocker) {
        this.msgScreenOnWakelockSent = false;
        this.msgPartialWakelockSent = false;
        HandlerThread ht = new HandlerThread(ATAG);
        ht.start();
        this.mHandler = new WorkerHandler(ht.getLooper());
        this.mSuspendBlocker = suspendBlocker;
        this.mCommonUtil = new CommonUtil(context);
        this.mPartialWakeLock = new OppoPartialWakeLockCheck(wakeLocks, lock, context, pms, this.mCommonUtil, ADBG, this.mHandler);
        this.mScreenOnWakeLock = new OppoScreenOnWakeLockCheck(wakeLocks, lock, context, pms, this.mCommonUtil, ADBG);
    }

    public boolean canSyncWakeLockAcq(int uid, String tag) {
        return this.mPartialWakeLock.canSyncWakeLockAcq(uid, tag);
    }

    public void PartialWakelockCheckStart() {
        if (!this.msgPartialWakelockSent) {
            this.msgPartialWakelockSent = true;
            this.mPartialWakeLock.clearSyncWakelock();
            this.mHandler.sendEmptyMessage(5);
        }
    }

    public void PartialWakelockCheckStop() {
        if (this.msgPartialWakelockSent) {
            this.msgPartialWakelockSent = false;
            this.mPartialWakeLock.clearSyncWakelock();
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void screenOnWakelockCheckStart() {
        if (!this.msgScreenOnWakelockSent) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), SCREEN_ON_WAKELOCK_CHECK_DELAY);
            this.msgScreenOnWakelockSent = true;
        }
    }

    public void screenOnWakelockCheckStop() {
        if (this.msgScreenOnWakelockSent) {
            this.mHandler.removeMessages(2);
            this.msgScreenOnWakelockSent = false;
        }
    }

    public void noteWakeLockChange(WakeLock wl, boolean acquire) {
        int msg;
        if (acquire) {
            msg = 3;
        } else {
            msg = 4;
        }
        this.mPartialWakeLock.noteWakeLockChange(wl, msg, this.mHandler, wl.mWorkSource);
    }

    public void noteWorkSourceChange(WakeLock wl, WorkSource newWorkSource) {
        this.mPartialWakeLock.noteWakeLockChange(wl, 4, this.mHandler, wl.mWorkSource);
        this.mPartialWakeLock.noteWakeLockChange(wl, 3, this.mHandler, newWorkSource);
    }

    public void dumpPossibleMusicPlayer(PrintWriter pw) {
        this.mPartialWakeLock.dumpPossibleMusicPlayer(pw);
    }

    public void logSwitch(boolean enable) {
        this.mPartialWakeLock.logSwitch(enable);
    }
}

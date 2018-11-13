package com.android.server.am;

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
class OppoBootCompleteBroadcast {
    private static final String ACTION_OPPO_BOOT_COMPLETED = "android.intent.action.OPPO_BOOT_COMPLETED";
    private static final int BROADCAST_COUNT_MIN = 20;
    static final int CHECK_IDLE_MSG = 400;
    private static final boolean DEBUG_OPPO_BT = false;
    private static final String FEATURE_OPPO_BOOT_COMPLETE = "oppo.ams.broadcast.oppobt";
    static final int MAX_COUNT_TO_CHECK = 3;
    private static final int MIN_SCORE = 30;
    static final String TAG = null;
    static final int TIME_DELAY_FOR_CHECK_IDLE = 30000;
    static final int TIME_DELAY_FOR_NEXT_CHECK = 40000;
    private static OppoBootCompleteBroadcast mInstance;
    final ActivityManagerService mAm;
    private final Runnable mCheckIdleCallBack;
    private boolean mEnableOppoBootComplete;
    private int mIdleCheckCount;
    private final UserController mUserController;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoBootCompleteBroadcast.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoBootCompleteBroadcast.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoBootCompleteBroadcast.<clinit>():void");
    }

    public static OppoBootCompleteBroadcast getInstance(ActivityManagerService service, UserController userController) {
        if (mInstance == null) {
            mInstance = new OppoBootCompleteBroadcast(service, userController);
        }
        return mInstance;
    }

    private OppoBootCompleteBroadcast(ActivityManagerService service, UserController userController) {
        this.mEnableOppoBootComplete = true;
        this.mIdleCheckCount = 0;
        this.mCheckIdleCallBack = new Runnable() {
            public void run() {
                OppoBootCompleteBroadcast oppoBootCompleteBroadcast = OppoBootCompleteBroadcast.this;
                oppoBootCompleteBroadcast.mIdleCheckCount = oppoBootCompleteBroadcast.mIdleCheckCount + 1;
                boolean sendOppoBtBroadcast = false;
                if (OppoBootCompleteBroadcast.this.isSystemIdle()) {
                    sendOppoBtBroadcast = true;
                } else if (OppoBootCompleteBroadcast.this.mIdleCheckCount >= 3) {
                    sendOppoBtBroadcast = true;
                } else {
                    OppoBootCompleteBroadcast.this.postCheckIdleCallBack(40000);
                }
                if (sendOppoBtBroadcast) {
                    OppoBootCompleteBroadcast.this.mUserController.sendOppoBootCompleteBroadcast();
                }
            }
        };
        this.mAm = service;
        this.mUserController = userController;
        this.mEnableOppoBootComplete = this.mAm.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_BOOT_COMPLETE);
    }

    public void triggerOppoBootcompleteBroadcast() {
        postCheckIdleCallBack(30000);
    }

    private void postCheckIdleCallBack(int delayTime) {
        if (this.mEnableOppoBootComplete) {
            if (delayTime > 0) {
                this.mAm.mHandler.postDelayed(this.mCheckIdleCallBack, (long) delayTime);
            } else if (delayTime == 0) {
                this.mAm.mHandler.post(this.mCheckIdleCallBack);
            }
        }
    }

    private boolean isSystemIdle() {
        int idleScore = 0;
        if (isBroadcastQueueIdle()) {
            idleScore = 20;
        }
        if (isProcStartIdle()) {
            idleScore += 10;
        }
        return idleScore >= 30;
    }

    private boolean isBroadcastQueueIdle() {
        return (this.mAm.mBgBroadcastQueue.mParallelBroadcasts.size() + this.mAm.mBgBroadcastQueue.mOrderedBroadcasts.size()) + (this.mAm.mFgBroadcastQueue.mParallelBroadcasts.size() + this.mAm.mFgBroadcastQueue.mOrderedBroadcasts.size()) <= 20;
    }

    private boolean isProcStartIdle() {
        return true;
    }
}

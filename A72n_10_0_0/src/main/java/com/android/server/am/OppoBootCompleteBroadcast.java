package com.android.server.am;

import com.color.util.ColorTypeCastingHelper;
import com.google.android.collect.Maps;
import java.util.Map;

/* access modifiers changed from: package-private */
public class OppoBootCompleteBroadcast {
    static final String ACTION_OPPO_BOOT_COMPLETED = "oppo.intent.action.BOOT_COMPLETED";
    private static final int BROADCAST_COUNT_MIN = 20;
    static final int CHECK_IDLE_MSG = 400;
    private static final boolean DEBUG_OPPO_BT = false;
    private static final String FEATURE_OPPO_BOOT_COMPLETE = "oppo.ams.broadcast.oppobt";
    static final int MAX_COUNT_TO_CHECK = 3;
    private static final int MIN_SCORE = 30;
    static final String TAG = "OppoBootCompleteBroadcast";
    static final int TIME_DELAY_FOR_CHECK_IDLE = 25000;
    static final int TIME_DELAY_FOR_NEXT_CHECK = 40000;
    private static OppoBootCompleteBroadcast mInstance = null;
    final ActivityManagerService mAm;
    private final Runnable mCheckIdleCallBack = new Runnable() {
        /* class com.android.server.am.OppoBootCompleteBroadcast.AnonymousClass1 */

        public void run() {
            OppoBaseUserController baseControler;
            OppoBootCompleteBroadcast.access$008(OppoBootCompleteBroadcast.this);
            boolean sendOppoBtBroadcast = false;
            if (OppoBootCompleteBroadcast.this.isSystemIdle()) {
                sendOppoBtBroadcast = true;
            } else if (OppoBootCompleteBroadcast.this.mIdleCheckCount >= 3) {
                sendOppoBtBroadcast = true;
            } else {
                OppoBootCompleteBroadcast.this.postCheckIdleCallBack(40000);
            }
            if (sendOppoBtBroadcast && (baseControler = (OppoBaseUserController) ColorTypeCastingHelper.typeCasting(OppoBaseUserController.class, OppoBootCompleteBroadcast.this.mUserController)) != null) {
                baseControler.sendOppoBootCompleteBroadcast();
            }
        }
    };
    private Map<Integer, CheckIdleCallBack> mCurUserIdleMap = Maps.newHashMap();
    private boolean mEnableOppoBootComplete = true;
    private int mIdleCheckCount = 0;
    private final UserController mUserController;

    static /* synthetic */ int access$008(OppoBootCompleteBroadcast x0) {
        int i = x0.mIdleCheckCount;
        x0.mIdleCheckCount = i + 1;
        return i;
    }

    public static OppoBootCompleteBroadcast getInstance(ActivityManagerService service, UserController userController) {
        if (mInstance == null) {
            mInstance = new OppoBootCompleteBroadcast(service, userController);
        }
        return mInstance;
    }

    private OppoBootCompleteBroadcast(ActivityManagerService service, UserController userController) {
        this.mAm = service;
        this.mUserController = userController;
        this.mEnableOppoBootComplete = this.mAm.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_BOOT_COMPLETE);
    }

    public void triggerOppoBootcompleteBroadcast() {
        postCheckIdleCallBack(TIME_DELAY_FOR_CHECK_IDLE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postCheckIdleCallBack(int delayTime) {
        if (this.mEnableOppoBootComplete) {
            if (delayTime > 0) {
                this.mAm.mHandler.postDelayed(this.mCheckIdleCallBack, (long) delayTime);
            } else if (delayTime == 0) {
                this.mAm.mHandler.post(this.mCheckIdleCallBack);
            }
        }
    }

    public void triggerOppoBootcompleteBroadcast(int userId) {
        postCheckIdleCallBack(TIME_DELAY_FOR_CHECK_IDLE, userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postCheckIdleCallBack(int delayTime, int userId) {
        if (this.mEnableOppoBootComplete) {
            CheckIdleCallBack runnable = this.mCurUserIdleMap.get(Integer.valueOf(userId));
            if (runnable == null) {
                runnable = new CheckIdleCallBack(userId);
                this.mCurUserIdleMap.put(Integer.valueOf(userId), runnable);
            }
            if (delayTime > 0) {
                this.mAm.mHandler.postDelayed(runnable, (long) delayTime);
            } else if (delayTime == 0) {
                this.mAm.mHandler.post(runnable);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSystemIdle() {
        int idleScore = 0;
        if (isBroadcastQueueIdle()) {
            idleScore = 0 + 20;
        }
        if (isProcStartIdle()) {
            idleScore += 10;
        }
        return idleScore >= 30;
    }

    private boolean isBroadcastQueueIdle() {
        return (this.mAm.mBgBroadcastQueue.mParallelBroadcasts.size() + typeCastToParent(this.mAm.mBgBroadcastQueue).getOrderedBroadcastsSize()) + (this.mAm.mFgBroadcastQueue.mParallelBroadcasts.size() + typeCastToParent(this.mAm.mFgBroadcastQueue).getOrderedBroadcastsSize()) <= 20;
    }

    private boolean isProcStartIdle() {
        return true;
    }

    private OppoBaseBroadcastQueue typeCastToParent(BroadcastQueue queue) {
        return (OppoBaseBroadcastQueue) ColorTypeCastingHelper.typeCasting(OppoBaseBroadcastQueue.class, queue);
    }

    /* access modifiers changed from: package-private */
    public class CheckIdleCallBack implements Runnable {
        private int idleCheckCount = 0;
        private int userId;

        public CheckIdleCallBack(int id) {
            this.userId = id;
        }

        public void run() {
            OppoBaseUserController baseControler;
            this.idleCheckCount++;
            boolean sendOppoBtBroadcast = false;
            if (OppoBootCompleteBroadcast.this.isSystemIdle()) {
                sendOppoBtBroadcast = true;
            } else if (this.idleCheckCount >= 3) {
                sendOppoBtBroadcast = true;
            } else {
                OppoBootCompleteBroadcast.this.postCheckIdleCallBack(40000, this.userId);
            }
            if (sendOppoBtBroadcast && (baseControler = (OppoBaseUserController) ColorTypeCastingHelper.typeCasting(OppoBaseUserController.class, OppoBootCompleteBroadcast.this.mUserController)) != null) {
                baseControler.sendOppoBootCompleteBroadcast(this.userId);
                OppoBootCompleteBroadcast.this.mCurUserIdleMap.remove(Integer.valueOf(this.userId));
            }
        }
    }
}

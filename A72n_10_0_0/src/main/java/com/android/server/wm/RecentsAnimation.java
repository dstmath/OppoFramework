package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IAssistDataReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Slog;
import android.view.IRecentsAnimationRunner;
import com.android.server.wm.ActivityDisplay;
import com.android.server.wm.RecentsAnimationController;
import com.oppo.hypnus.HypnusManager;

/* access modifiers changed from: package-private */
public class RecentsAnimation implements RecentsAnimationController.RecentsAnimationCallbacks, ActivityDisplay.OnStackOrderChangedListener {
    private static final boolean DEBUG = WindowManagerDebugConfig.DEBUG_RECENTS_ANIMATIONS;
    private static final String TAG = RecentsAnimation.class.getSimpleName();
    private final ActivityStartController mActivityStartController;
    private final int mCallingPid;
    private final ActivityDisplay mDefaultDisplay = this.mService.mRootActivityContainer.getDefaultDisplay();
    private HypnusManager mHypnusManager;
    private ActivityRecord mLaunchedTargetActivity;
    private ActivityStack mRestoreTargetBehindStack;
    private final ActivityTaskManagerService mService;
    private final ActivityStackSupervisor mStackSupervisor;
    private int mTargetActivityType;
    private final WindowManagerService mWindowManager;

    RecentsAnimation(ActivityTaskManagerService atm, ActivityStackSupervisor stackSupervisor, ActivityStartController activityStartController, WindowManagerService wm, int callingPid) {
        this.mService = atm;
        this.mStackSupervisor = stackSupervisor;
        this.mActivityStartController = activityStartController;
        this.mWindowManager = wm;
        this.mCallingPid = callingPid;
        this.mHypnusManager = new HypnusManager();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00ac  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x010c A[SYNTHETIC, Splitter:B:36:0x010c] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0156 A[SYNTHETIC, Splitter:B:49:0x0156] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01fe A[Catch:{ Exception -> 0x024e, all -> 0x024a }] */
    public void startRecentsActivity(Intent intent, IRecentsAnimationRunner recentsAnimationRunner, ComponentName recentsComponent, int recentsUid, @Deprecated IAssistDataReceiver assistDataReceiver) {
        int i;
        HypnusManager hypnusManager;
        ActivityRecord targetActivity;
        boolean hasExistingActivity;
        Exception e;
        Exception e2;
        ActivityOptions options;
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "startRecentsActivity(): intent=" + intent + " assistDataReceiver=" + assistDataReceiver);
        }
        Trace.traceBegin(64, "RecentsAnimation#startRecentsActivity");
        DisplayContent dc = this.mService.mRootActivityContainer.getDefaultDisplay().mDisplayContent;
        if (!this.mWindowManager.canStartRecentsAnimation()) {
            notifyAnimationCancelBeforeStart(recentsAnimationRunner);
            if (DEBUG) {
                String str2 = TAG;
                Slog.d(str2, "Can't start recents animation, nextAppTransition=" + dc.mAppTransition.getAppTransition());
                return;
            }
            return;
        }
        int userId = this.mService.getCurrentUserId();
        if (intent.getComponent() != null) {
            if (recentsComponent.equals(intent.getComponent())) {
                i = 3;
                this.mTargetActivityType = i;
                hypnusManager = this.mHypnusManager;
                if (hypnusManager != null) {
                    hypnusManager.hypnusSetAction(12, 1000);
                }
                int i2 = 0;
                ActivityStack targetStack = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
                targetActivity = getTargetActivity(targetStack, intent.getComponent(), userId);
                hasExistingActivity = targetActivity == null;
                if (hasExistingActivity) {
                    this.mRestoreTargetBehindStack = targetActivity.getDisplay().getStackAbove(targetStack);
                    if (this.mRestoreTargetBehindStack == null) {
                        notifyAnimationCancelBeforeStart(recentsAnimationRunner);
                        if (DEBUG) {
                            String str3 = TAG;
                            Slog.d(str3, "No stack above target stack=" + targetStack);
                            return;
                        }
                        return;
                    }
                }
                if (targetActivity == null || !targetActivity.visible) {
                    this.mService.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(true, targetActivity);
                }
                this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
                this.mService.mH.post(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$RecentsAnimation$e3kosml870P6Bh_K_Z_6yyLHZk */

                    public final void run() {
                        RecentsAnimation.this.lambda$startRecentsActivity$0$RecentsAnimation();
                    }
                });
                this.mWindowManager.deferSurfaceLayout();
                if (!hasExistingActivity) {
                    try {
                        this.mDefaultDisplay.moveStackBehindBottomMostVisibleStack(targetStack);
                        if (DEBUG) {
                            String str4 = TAG;
                            Slog.d(str4, "Moved stack=" + targetStack + " behind stack=" + this.mDefaultDisplay.getStackAbove(targetStack));
                        }
                        if (targetStack.topTask() != targetActivity.getTaskRecord()) {
                            targetStack.addTask(targetActivity.getTaskRecord(), true, "startRecentsActivity");
                        }
                    } catch (Exception e3) {
                        e2 = e3;
                        try {
                            Slog.e(TAG, "Failed to start recents activity", e2);
                            throw e2;
                        } catch (Throwable th) {
                            e = th;
                            this.mWindowManager.continueSurfaceLayout();
                            Trace.traceEnd(64);
                            throw e;
                        }
                    } catch (Throwable th2) {
                        e = th2;
                        this.mWindowManager.continueSurfaceLayout();
                        Trace.traceEnd(64);
                        throw e;
                    }
                } else {
                    try {
                        options = ActivityOptions.makeBasic();
                        options.setLaunchActivityType(this.mTargetActivityType);
                        options.setAvoidMoveToFront();
                        intent.addFlags(268500992);
                    } catch (Exception e4) {
                        e2 = e4;
                        Slog.e(TAG, "Failed to start recents activity", e2);
                        throw e2;
                    } catch (Throwable th3) {
                        e = th3;
                        this.mWindowManager.continueSurfaceLayout();
                        Trace.traceEnd(64);
                        throw e;
                    }
                    try {
                        this.mActivityStartController.obtainStarter(intent, "startRecentsActivity_noTargetActivity").setCallingUid(recentsUid).setCallingPackage(recentsComponent.getPackageName()).setActivityOptions(SafeActivityOptions.fromBundle(options.toBundle())).setMayWait(userId).execute();
                        try {
                            targetStack = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
                            try {
                                targetActivity = getTargetActivity(targetStack, intent.getComponent(), userId);
                                this.mDefaultDisplay.moveStackBehindBottomMostVisibleStack(targetStack);
                                if (DEBUG) {
                                    String str5 = TAG;
                                    Slog.d(str5, "Moved stack=" + targetStack + " behind stack=" + this.mDefaultDisplay.getStackAbove(targetStack));
                                }
                                i2 = 0;
                                this.mWindowManager.prepareAppTransition(0, false);
                                this.mWindowManager.executeAppTransition();
                                if (DEBUG) {
                                    String str6 = TAG;
                                    Slog.d(str6, "Started intent=" + intent);
                                }
                            } catch (Exception e5) {
                                e2 = e5;
                                Slog.e(TAG, "Failed to start recents activity", e2);
                                throw e2;
                            }
                        } catch (Exception e6) {
                            e2 = e6;
                            Slog.e(TAG, "Failed to start recents activity", e2);
                            throw e2;
                        } catch (Throwable th4) {
                            e = th4;
                            this.mWindowManager.continueSurfaceLayout();
                            Trace.traceEnd(64);
                            throw e;
                        }
                    } catch (Exception e7) {
                        e2 = e7;
                        Slog.e(TAG, "Failed to start recents activity", e2);
                        throw e2;
                    } catch (Throwable th5) {
                        e = th5;
                        this.mWindowManager.continueSurfaceLayout();
                        Trace.traceEnd(64);
                        throw e;
                    }
                }
                targetActivity.mLaunchTaskBehind = true;
                this.mLaunchedTargetActivity = targetActivity;
                if (DEBUG) {
                    Slog.d(TAG, "cancel last animation, then initialize new animation");
                }
                this.mWindowManager.cancelRecentsAnimationSynchronously(2, "startRecentsActivity");
                this.mWindowManager.initializeRecentsAnimation(this.mTargetActivityType, recentsAnimationRunner, this, this.mDefaultDisplay.mDisplayId, this.mStackSupervisor.mRecentTasks.getRecentTaskIds());
                this.mService.mRootActivityContainer.ensureActivitiesVisible(null, i2, true);
                this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunched(2, targetActivity);
                this.mDefaultDisplay.registerStackOrderChangedListener(this);
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
        i = 2;
        this.mTargetActivityType = i;
        hypnusManager = this.mHypnusManager;
        if (hypnusManager != null) {
        }
        int i22 = 0;
        ActivityStack targetStack2 = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
        targetActivity = getTargetActivity(targetStack2, intent.getComponent(), userId);
        if (targetActivity == null) {
        }
        if (hasExistingActivity) {
        }
        this.mService.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(true, targetActivity);
        this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
        this.mService.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$RecentsAnimation$e3kosml870P6Bh_K_Z_6yyLHZk */

            public final void run() {
                RecentsAnimation.this.lambda$startRecentsActivity$0$RecentsAnimation();
            }
        });
        this.mWindowManager.deferSurfaceLayout();
        if (!hasExistingActivity) {
        }
        try {
            targetActivity.mLaunchTaskBehind = true;
            this.mLaunchedTargetActivity = targetActivity;
            if (DEBUG) {
            }
            this.mWindowManager.cancelRecentsAnimationSynchronously(2, "startRecentsActivity");
            this.mWindowManager.initializeRecentsAnimation(this.mTargetActivityType, recentsAnimationRunner, this, this.mDefaultDisplay.mDisplayId, this.mStackSupervisor.mRecentTasks.getRecentTaskIds());
            this.mService.mRootActivityContainer.ensureActivitiesVisible(null, i22, true);
            this.mStackSupervisor.getActivityMetricsLogger().notifyActivityLaunched(2, targetActivity);
            this.mDefaultDisplay.registerStackOrderChangedListener(this);
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
        } catch (Exception e8) {
            e2 = e8;
            Slog.e(TAG, "Failed to start recents activity", e2);
            throw e2;
        } catch (Throwable th6) {
            e = th6;
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
            throw e;
        }
    }

    public /* synthetic */ void lambda$startRecentsActivity$0$RecentsAnimation() {
        this.mService.mAmInternal.setRunningRemoteAnimation(this.mCallingPid, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: finishAnimation */
    public void lambda$onAnimationFinished$3$RecentsAnimation(@RecentsAnimationController.ReorderMode int reorderMode, boolean sendUserLeaveHint) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mHypnusManager != null) {
                    this.mHypnusManager.hypnusSetAction(12, 1000);
                }
                if (DEBUG) {
                    String str = TAG;
                    Slog.d(str, "onAnimationFinished(): controller=" + this.mWindowManager.getRecentsAnimationController() + " reorderMode=" + reorderMode);
                }
                this.mDefaultDisplay.unregisterStackOrderChangedListener(this);
                RecentsAnimationController controller = this.mWindowManager.getRecentsAnimationController();
                if (controller != null) {
                    if (reorderMode != 0) {
                        this.mService.mRootActivityContainer.sendPowerHintForLaunchEndIfNeeded();
                    }
                    if (reorderMode == 1) {
                        this.mService.stopAppSwitches();
                    }
                    this.mService.mH.post(new Runnable() {
                        /* class com.android.server.wm.$$Lambda$RecentsAnimation$maWFdpvN04gpjsVfJu49wyo8hQ */

                        public final void run() {
                            RecentsAnimation.this.lambda$finishAnimation$1$RecentsAnimation();
                        }
                    });
                    this.mWindowManager.inSurfaceTransaction(new Runnable(reorderMode, sendUserLeaveHint, controller) {
                        /* class com.android.server.wm.$$Lambda$RecentsAnimation$t0H9VDhk8jOhDLGudyjnaASceuk */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ boolean f$2;
                        private final /* synthetic */ RecentsAnimationController f$3;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        public final void run() {
                            RecentsAnimation.this.lambda$finishAnimation$2$RecentsAnimation(this.f$1, this.f$2, this.f$3);
                        }
                    });
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public /* synthetic */ void lambda$finishAnimation$1$RecentsAnimation() {
        this.mService.mAmInternal.setRunningRemoteAnimation(this.mCallingPid, false);
    }

    public /* synthetic */ void lambda$finishAnimation$2$RecentsAnimation(int reorderMode, boolean sendUserLeaveHint, RecentsAnimationController controller) {
        ActivityRecord targetActivity;
        ActivityStack topStack;
        Trace.traceBegin(64, "RecentsAnimation#onAnimationFinished_inSurfaceTransaction");
        this.mWindowManager.deferSurfaceLayout();
        try {
            this.mWindowManager.cleanupRecentsAnimation(reorderMode);
            ActivityStack targetStack = this.mDefaultDisplay.getStack(0, this.mTargetActivityType);
            if (targetStack != null) {
                targetActivity = targetStack.isInStackLocked(this.mLaunchedTargetActivity);
            } else {
                targetActivity = null;
            }
            if (DEBUG) {
                Slog.d(TAG, "onAnimationFinished(): targetStack=" + targetStack + " targetActivity=" + targetActivity + " mRestoreTargetBehindStack=" + this.mRestoreTargetBehindStack);
            }
            if (targetActivity == null) {
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                return;
            }
            targetActivity.mLaunchTaskBehind = false;
            if (reorderMode == 1) {
                this.mStackSupervisor.mNoAnimActivities.add(targetActivity);
                if (targetStack.isHomeOrRecentsStack()) {
                    this.mService.onStartActivitySetDidAppSwitch();
                }
                if (sendUserLeaveHint) {
                    this.mStackSupervisor.mUserLeaving = true;
                    targetStack.moveTaskToFrontLocked(targetActivity.getTaskRecord(), true, null, targetActivity.appTimeTracker, "RecentsAnimation.onAnimationFinished()");
                } else {
                    targetStack.moveToFront("RecentsAnimation.onAnimationFinished()");
                }
                if (DEBUG && (topStack = getTopNonAlwaysOnTopStack()) != targetStack) {
                    Slog.w(TAG, "Expected target stack=" + targetStack + " to be top most but found stack=" + topStack);
                }
            } else if (reorderMode == 2) {
                targetActivity.getDisplay().moveStackBehindStack(targetStack, this.mRestoreTargetBehindStack);
                if (DEBUG) {
                    ActivityStack aboveTargetStack = this.mDefaultDisplay.getStackAbove(targetStack);
                    if (!(this.mRestoreTargetBehindStack == null || aboveTargetStack == this.mRestoreTargetBehindStack)) {
                        Slog.w(TAG, "Expected target stack=" + targetStack + " to restored behind stack=" + this.mRestoreTargetBehindStack + " but it is behind stack=" + aboveTargetStack);
                    }
                }
            } else {
                String msg = "";
                if (!controller.shouldCancelWithDeferredScreenshot() && !targetStack.isFocusedStackOnDisplay()) {
                    targetStack.ensureActivitiesVisibleLocked(null, 0, false);
                    if (DEBUG) {
                        msg = "ensure activities visible";
                    }
                }
                if (DEBUG) {
                    Slog.d(TAG, "onAnimationFinished -> reorderMode is REORDER_KEEP_IN_PLACE, " + msg + " then return");
                }
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                return;
            }
            this.mWindowManager.prepareAppTransition(0, false);
            this.mService.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
            this.mService.mRootActivityContainer.resumeFocusedStacksTopActivities();
            this.mWindowManager.executeAppTransition();
            this.mWindowManager.checkSplitScreenMinimizedChanged(true);
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
        } catch (Exception e) {
            Slog.e(TAG, "Failed to clean up recents activity", e);
            throw e;
        } catch (Throwable th) {
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
            throw th;
        }
    }

    @Override // com.android.server.wm.RecentsAnimationController.RecentsAnimationCallbacks
    public void onAnimationFinished(@RecentsAnimationController.ReorderMode int reorderMode, boolean runSychronously, boolean sendUserLeaveHint) {
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "onAnimationFinished -> reorderMode = " + reorderMode + ", runSychronously = " + runSychronously + ", sendUserLeaveHint = " + sendUserLeaveHint);
        }
        if (runSychronously) {
            lambda$onAnimationFinished$3$RecentsAnimation(reorderMode, sendUserLeaveHint);
        } else {
            this.mService.mH.post(new Runnable(reorderMode, sendUserLeaveHint) {
                /* class com.android.server.wm.$$Lambda$RecentsAnimation$yp3SVPfM17AJdya7PiWVlmTQumE */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    RecentsAnimation.this.lambda$onAnimationFinished$3$RecentsAnimation(this.f$1, this.f$2);
                }
            });
        }
    }

    @Override // com.android.server.wm.ActivityDisplay.OnStackOrderChangedListener
    public void onStackOrderChanged(ActivityStack stack) {
        RecentsAnimationController controller;
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "onStackOrderChanged(): stack=" + stack);
        }
        if (this.mDefaultDisplay.getIndexOf(stack) != -1 && stack.shouldBeVisible(null) && (controller = this.mWindowManager.getRecentsAnimationController()) != null) {
            this.mService.mRootActivityContainer.getDefaultDisplay().mDisplayContent.mBoundsAnimationController.setAnimationType(controller.shouldCancelWithDeferredScreenshot() ? 1 : 0);
            if ((!controller.isAnimatingTask((Task) stack.getTaskStack().getTopChild()) || (stack.getTopActivity() != null && controller.isTargetApp(stack.getTopActivity().mAppWindowToken))) && controller.shouldCancelWithDeferredScreenshot()) {
                controller.cancelOnNextTransitionStart();
            } else {
                this.mWindowManager.cancelRecentsAnimationSynchronously(0, "stackOrderChanged");
            }
        }
    }

    private void notifyAnimationCancelBeforeStart(IRecentsAnimationRunner recentsAnimationRunner) {
        if (DEBUG) {
            Slog.d(TAG, "notifyAnimationCancelBeforeStart()");
        }
        try {
            recentsAnimationRunner.onAnimationCanceled(false);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to cancel recents animation before start", e);
        }
    }

    private ActivityStack getTopNonAlwaysOnTopStack() {
        for (int i = this.mDefaultDisplay.getChildCount() - 1; i >= 0; i--) {
            ActivityStack s = this.mDefaultDisplay.getChildAt(i);
            if (!s.getWindowConfiguration().isAlwaysOnTop()) {
                return s;
            }
        }
        return null;
    }

    private ActivityRecord getTargetActivity(ActivityStack targetStack, ComponentName component, int userId) {
        if (targetStack == null) {
            return null;
        }
        for (int i = targetStack.getChildCount() - 1; i >= 0; i--) {
            TaskRecord task = targetStack.getChildAt(i);
            if (task.userId == userId && task.getBaseIntent().getComponent().equals(component)) {
                return task.getTopActivity();
            }
        }
        return null;
    }
}

package com.android.server.wm;

import android.content.Context;
import android.os.Trace;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.SurfaceControl;
import com.android.server.AnimationThread;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WindowAnimator {
    private static final String TAG = "WindowManager";
    private final ArrayList<Runnable> mAfterPrepareSurfacesRunnables = new ArrayList<>();
    private boolean mAnimating;
    final Choreographer.FrameCallback mAnimationFrameCallback;
    private boolean mAnimationFrameCallbackScheduled;
    int mBulkUpdateParams = 0;
    private Choreographer mChoreographer;
    final Context mContext;
    long mCurrentTime;
    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators = new SparseArray<>(2);
    private boolean mInExecuteAfterPrepareSurfacesRunnables;
    private boolean mInitialized = false;
    private boolean mLastRootAnimating;
    Object mLastWindowFreezeSource;
    final WindowManagerPolicy mPolicy;
    private boolean mRemoveReplacedWindows = false;
    final WindowManagerService mService;
    private final SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();

    WindowAnimator(WindowManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        this.mPolicy = service.mPolicy;
        AnimationThread.getHandler().runWithScissors(new Runnable() {
            /* class com.android.server.wm.$$Lambda$WindowAnimator$U3Fu5_RzEyNo8Jt6zTb2ozdXiqM */

            public final void run() {
                WindowAnimator.this.lambda$new$0$WindowAnimator();
            }
        }, 0);
        this.mAnimationFrameCallback = new Choreographer.FrameCallback() {
            /* class com.android.server.wm.$$Lambda$WindowAnimator$ddXU8gK8rmDqri0OZVMNa3Y4GHk */

            public final void doFrame(long j) {
                WindowAnimator.this.lambda$new$1$WindowAnimator(j);
            }
        };
    }

    public /* synthetic */ void lambda$new$0$WindowAnimator() {
        this.mChoreographer = Choreographer.getSfInstance();
    }

    /* JADX INFO: finally extract failed */
    public /* synthetic */ void lambda$new$1$WindowAnimator(long frameTimeNs) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAnimationFrameCallbackScheduled = false;
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Trace.traceBegin(32, "wmAnimate");
        animate(frameTimeNs);
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public void addDisplayLocked(int displayId) {
        getDisplayContentsAnimatorLocked(displayId);
    }

    /* access modifiers changed from: package-private */
    public void removeDisplayLocked(int displayId) {
        DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.get(displayId);
        if (!(displayAnimator == null || displayAnimator.mScreenRotationAnimation == null)) {
            displayAnimator.mScreenRotationAnimation.kill();
            displayAnimator.mScreenRotationAnimation = null;
        }
        this.mDisplayContentsAnimators.delete(displayId);
    }

    /* access modifiers changed from: package-private */
    public void ready() {
        this.mInitialized = true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r3 = r15.mService.mGlobalLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
        r15.mCurrentTime = r16 / 1000000;
        r15.mBulkUpdateParams = 4;
        r15.mAnimating = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE == false) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        android.util.Slog.i("WindowManager", "!!! animate: entry time=" + r15.mCurrentTime);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS == false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004e, code lost:
        android.util.Slog.i("WindowManager", ">>> OPEN TRANSACTION animate");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        r15.mService.openSurfaceTransaction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0 = r15.mService.mAccessibilityController;
        r5 = r15.mDisplayContentsAnimators.size();
        r6 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0066, code lost:
        if (r6 >= r5) goto L_0x00dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0068, code lost:
        r8 = r15.mService.mRoot.getDisplayContent(r15.mDisplayContentsAnimators.keyAt(r6));
        r9 = r15.mDisplayContentsAnimators.valueAt(r6);
        r10 = r9.mScreenRotationAnimation;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0080, code lost:
        if (r10 == null) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0086, code lost:
        if (r10.isAnimating() == false) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008e, code lost:
        if (r10.stepAnimationLocked(r15.mCurrentTime) == false) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0090, code lost:
        setAnimating(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0094, code lost:
        r15.mBulkUpdateParams |= 1;
        r10.kill();
        r9.mScreenRotationAnimation = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009f, code lost:
        if (r0 == null) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a1, code lost:
        r0.onRotationChangedLocked(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b0, code lost:
        if (android.common.OppoFeatureCache.get(com.android.server.wm.IColorBreenoManager.DEFAULT).hasColorDragWindowAnimation() == false) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b2, code lost:
        r15.mAnimating |= android.common.OppoFeatureCache.get(com.android.server.wm.IColorBreenoManager.DEFAULT).stepAnimation(r15.mCurrentTime);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c6, code lost:
        android.common.OppoFeatureCache.get(com.android.server.wm.IColorBreenoManager.DEFAULT).recoveryState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d1, code lost:
        r8.updateWindowsForAnimator();
        r8.updateBackgroundForAnimator();
        r8.prepareSurfaces();
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00dd, code lost:
        r6 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00de, code lost:
        if (r6 >= r5) goto L_0x0117;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00e0, code lost:
        r7 = r15.mDisplayContentsAnimators.keyAt(r6);
        r8 = r15.mService.mRoot.getDisplayContent(r7);
        r8.checkAppWindowsReadyToShow();
        r9 = r15.mDisplayContentsAnimators.valueAt(r6).mScreenRotationAnimation;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00fb, code lost:
        if (r9 == null) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00fd, code lost:
        r9.updateSurfaces(r15.mTransaction);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0102, code lost:
        orAnimating(r8.getDockedDividerController().animate(r15.mCurrentTime));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x010f, code lost:
        if (r0 == null) goto L_0x0114;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0111, code lost:
        r0.drawMagnifiedRegionBorderIfNeededLocked(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0114, code lost:
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0119, code lost:
        if (r15.mAnimating != false) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011b, code lost:
        cancelAnimation();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0122, code lost:
        if (r15.mService.mWatermark == null) goto L_0x012b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0124, code lost:
        r15.mService.mWatermark.drawIfNeeded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x012b, code lost:
        android.common.OppoFeatureCache.get(com.android.server.wm.IColorWatermarkManager.DEFAULT).draw();
        android.view.SurfaceControl.mergeToGlobalTransaction(r15.mTransaction);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x013b, code lost:
        r15.mService.closeSurfaceTransaction("WindowAnimator");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0144, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS == false) goto L_0x016a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0146, code lost:
        r0 = "WindowManager";
        r5 = "<<< CLOSE TRANSACTION animate";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x014a, code lost:
        android.util.Slog.i(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x014e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0151, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        android.util.Slog.wtf("WindowManager", "Unhandled exception in Window Manager", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0159, code lost:
        r15.mService.closeSurfaceTransaction("WindowAnimator");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0163, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS != false) goto L_0x0165;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0165, code lost:
        r0 = "WindowManager";
        r5 = "<<< CLOSE TRANSACTION animate";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x016a, code lost:
        r0 = r15.mService.mRoot.hasPendingLayoutChanges(r15);
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0175, code lost:
        if (r15.mBulkUpdateParams != 0) goto L_0x0177;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0177, code lost:
        r5 = r15.mService.mRoot.copyAnimToLayoutParams();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0184, code lost:
        r15.mService.mWindowPlacerLocked.requestTraversal();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x018b, code lost:
        r6 = r15.mService.mRoot.isSelfOrChildAnimating();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x019b, code lost:
        r15.mService.mTaskSnapshotController.setPersisterPaused(true);
        android.os.Trace.asyncTraceBegin(32, "animating", 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01ad, code lost:
        r15.mService.mWindowPlacerLocked.requestTraversal();
        r15.mService.mTaskSnapshotController.setPersisterPaused(false);
        android.os.Trace.asyncTraceEnd(32, "animating", 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01c0, code lost:
        r15.mLastRootAnimating = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01c4, code lost:
        if (r15.mRemoveReplacedWindows != false) goto L_0x01c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01c6, code lost:
        r15.mService.mRoot.removeReplacedWindows();
        r15.mRemoveReplacedWindows = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01cf, code lost:
        r15.mService.destroyPreservedSurfaceLocked();
        executeAfterPrepareSurfacesRunnables();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01d9, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE != false) goto L_0x01db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01db, code lost:
        android.util.Slog.i("WindowManager", "!!! animate: exit mAnimating=" + r15.mAnimating + " mBulkUpdateParams=" + java.lang.Integer.toHexString(r15.mBulkUpdateParams) + " hasPendingLayoutChanges=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0209, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x020d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x020e, code lost:
        r15.mService.closeSurfaceTransaction("WindowAnimator");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0217, code lost:
        if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS != false) goto L_0x0219;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0219, code lost:
        android.util.Slog.i("WindowManager", "<<< CLOSE TRANSACTION animate");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0220, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0221, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0223, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0226, code lost:
        throw r0;
     */
    private void animate(long frameTimeNs) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mInitialized) {
                    scheduleAnimation();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private static String bulkUpdateParamsToString(int bulkUpdateParams) {
        StringBuilder builder = new StringBuilder(128);
        if ((bulkUpdateParams & 1) != 0) {
            builder.append(" UPDATE_ROTATION");
        }
        if ((bulkUpdateParams & 4) != 0) {
            builder.append(" ORIENTATION_CHANGE_COMPLETE");
        }
        return builder.toString();
    }

    public void dumpLocked(PrintWriter pw, String prefix, boolean dumpAll) {
        String subPrefix = "  " + prefix;
        String subSubPrefix = "  " + subPrefix;
        for (int i = 0; i < this.mDisplayContentsAnimators.size(); i++) {
            pw.print(prefix);
            pw.print("DisplayContentsAnimator #");
            pw.print(this.mDisplayContentsAnimators.keyAt(i));
            pw.println(":");
            DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.valueAt(i);
            this.mService.mRoot.getDisplayContent(this.mDisplayContentsAnimators.keyAt(i)).dumpWindowAnimators(pw, subPrefix);
            if (displayAnimator.mScreenRotationAnimation != null) {
                pw.print(subPrefix);
                pw.println("mScreenRotationAnimation:");
                displayAnimator.mScreenRotationAnimation.printTo(subSubPrefix, pw);
            } else if (dumpAll) {
                pw.print(subPrefix);
                pw.println("no ScreenRotationAnimation ");
            }
            pw.println();
        }
        pw.println();
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mCurrentTime=");
            pw.println(TimeUtils.formatUptime(this.mCurrentTime));
        }
        if (this.mBulkUpdateParams != 0) {
            pw.print(prefix);
            pw.print("mBulkUpdateParams=0x");
            pw.print(Integer.toHexString(this.mBulkUpdateParams));
            pw.println(bulkUpdateParamsToString(this.mBulkUpdateParams));
        }
    }

    /* access modifiers changed from: package-private */
    public int getPendingLayoutChanges(int displayId) {
        DisplayContent displayContent;
        if (displayId >= 0 && (displayContent = this.mService.mRoot.getDisplayContent(displayId)) != null) {
            return displayContent.pendingLayoutChanges;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setPendingLayoutChanges(int displayId, int changes) {
        DisplayContent displayContent;
        if (displayId >= 0 && (displayContent = this.mService.mRoot.getDisplayContent(displayId)) != null) {
            displayContent.pendingLayoutChanges |= changes;
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator displayAnimator = this.mDisplayContentsAnimators.get(displayId);
        if (displayAnimator != null || this.mService.mRoot.getDisplayContent(displayId) == null) {
            return displayAnimator;
        }
        DisplayContentsAnimator displayAnimator2 = new DisplayContentsAnimator();
        this.mDisplayContentsAnimators.put(displayId, displayAnimator2);
        return displayAnimator2;
    }

    /* access modifiers changed from: package-private */
    public void setScreenRotationAnimationLocked(int displayId, ScreenRotationAnimation animation) {
        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        if (animator != null) {
            animator.mScreenRotationAnimation = animation;
        }
    }

    /* access modifiers changed from: package-private */
    public ScreenRotationAnimation getScreenRotationAnimationLocked(int displayId) {
        DisplayContentsAnimator animator;
        if (displayId >= 0 && (animator = getDisplayContentsAnimatorLocked(displayId)) != null) {
            return animator.mScreenRotationAnimation;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void requestRemovalOfReplacedWindows(WindowState win) {
        this.mRemoveReplacedWindows = true;
    }

    /* access modifiers changed from: package-private */
    public void scheduleAnimation() {
        if (!this.mAnimationFrameCallbackScheduled) {
            this.mAnimationFrameCallbackScheduled = true;
            this.mChoreographer.postFrameCallback(this.mAnimationFrameCallback);
        }
    }

    private void cancelAnimation() {
        if (this.mAnimationFrameCallbackScheduled) {
            this.mAnimationFrameCallbackScheduled = false;
            this.mChoreographer.removeFrameCallback(this.mAnimationFrameCallback);
        }
    }

    /* access modifiers changed from: private */
    public class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation;

        private DisplayContentsAnimator() {
            this.mScreenRotationAnimation = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimating() {
        return this.mAnimating;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimationScheduled() {
        return this.mAnimationFrameCallbackScheduled;
    }

    /* access modifiers changed from: package-private */
    public Choreographer getChoreographer() {
        return this.mChoreographer;
    }

    /* access modifiers changed from: package-private */
    public void setAnimating(boolean animating) {
        this.mAnimating = animating;
    }

    /* access modifiers changed from: package-private */
    public void orAnimating(boolean animating) {
        this.mAnimating |= animating;
    }

    /* access modifiers changed from: package-private */
    public void addAfterPrepareSurfacesRunnable(Runnable r) {
        if (this.mInExecuteAfterPrepareSurfacesRunnables) {
            r.run();
            return;
        }
        this.mAfterPrepareSurfacesRunnables.add(r);
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public void executeAfterPrepareSurfacesRunnables() {
        if (!this.mInExecuteAfterPrepareSurfacesRunnables) {
            this.mInExecuteAfterPrepareSurfacesRunnables = true;
            int size = this.mAfterPrepareSurfacesRunnables.size();
            for (int i = 0; i < size; i++) {
                this.mAfterPrepareSurfacesRunnables.get(i).run();
            }
            this.mAfterPrepareSurfacesRunnables.clear();
            this.mInExecuteAfterPrepareSurfacesRunnables = false;
        }
    }
}

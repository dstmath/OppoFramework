package com.android.server.wm;

import android.content.Context;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import android.view.WindowManagerPolicy;
import com.android.server.AnimationThread;
import java.io.PrintWriter;

public class WindowAnimator {
    private static final String TAG = "WindowManager";
    int mAnimTransactionSequence;
    private boolean mAnimating;
    final FrameCallback mAnimationFrameCallback;
    private boolean mAnimationFrameCallbackScheduled;
    boolean mAppWindowAnimating;
    int mBulkUpdateParams = 0;
    private Choreographer mChoreographer;
    final Context mContext;
    long mCurrentTime;
    SparseArray<DisplayContentsAnimator> mDisplayContentsAnimators = new SparseArray(2);
    boolean mInitialized = false;
    private boolean mLastAnimating;
    Object mLastWindowFreezeSource;
    final WindowManagerPolicy mPolicy;
    private boolean mRemoveReplacedWindows = false;
    final WindowManagerService mService;
    WindowState mWindowDetachedWallpaper = null;
    private final WindowSurfacePlacer mWindowPlacerLocked;

    private class DisplayContentsAnimator {
        ScreenRotationAnimation mScreenRotationAnimation;

        /* synthetic */ DisplayContentsAnimator(WindowAnimator this$0, DisplayContentsAnimator -this1) {
            this();
        }

        private DisplayContentsAnimator() {
            this.mScreenRotationAnimation = null;
        }
    }

    WindowAnimator(WindowManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        this.mPolicy = service.mPolicy;
        this.mWindowPlacerLocked = service.mWindowPlacerLocked;
        AnimationThread.getHandler().runWithScissors(new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 12, this), 0);
        this.mAnimationFrameCallback = new -$Lambda$OQfQhd_xsxt9hoLAjIbVfOwa-jY(this);
    }

    /* renamed from: lambda$-com_android_server_wm_WindowAnimator_3844 */
    /* synthetic */ void m255lambda$-com_android_server_wm_WindowAnimator_3844() {
        this.mChoreographer = Choreographer.getSfInstance();
    }

    /* renamed from: lambda$-com_android_server_wm_WindowAnimator_3951 */
    /* synthetic */ void m256lambda$-com_android_server_wm_WindowAnimator_3951(long frameTimeNs) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAnimationFrameCallbackScheduled = false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        animate(frameTimeNs);
    }

    void addDisplayLocked(int displayId) {
        getDisplayContentsAnimatorLocked(displayId);
        if (displayId == 0) {
            this.mInitialized = true;
        }
    }

    void removeDisplayLocked(int displayId) {
        DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.get(displayId);
        if (!(displayAnimator == null || displayAnimator.mScreenRotationAnimation == null)) {
            displayAnimator.mScreenRotationAnimation.kill();
            displayAnimator.mScreenRotationAnimation = null;
        }
        this.mDisplayContentsAnimators.delete(displayId);
    }

    /* JADX WARNING: Missing block: B:10:0x0019, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
            r19.mService.executeEmptyAnimationTransaction();
            r13 = r19.mService.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:11:0x0029, code:
            monitor-enter(r13);
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
            r19.mCurrentTime = r20 / 1000000;
            r19.mBulkUpdateParams = 8;
            r19.mAnimating = false;
            r19.mAppWindowAnimating = false;
     */
    /* JADX WARNING: Missing block: B:14:0x0048, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE == false) goto L_0x006b;
     */
    /* JADX WARNING: Missing block: B:15:0x004a, code:
            android.util.Slog.i(TAG, "!!! animate: entry time=" + r19.mCurrentTime);
     */
    /* JADX WARNING: Missing block: B:17:0x006d, code:
            if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS == false) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:18:0x006f, code:
            android.util.Slog.i(TAG, ">>> OPEN TRANSACTION animate");
     */
    /* JADX WARNING: Missing block: B:19:0x0077, code:
            r19.mService.openSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r2 = r19.mService.mAccessibilityController;
            r10 = r19.mDisplayContentsAnimators.size();
            r9 = 0;
     */
    /* JADX WARNING: Missing block: B:22:0x008d, code:
            if (r9 >= r10) goto L_0x0221;
     */
    /* JADX WARNING: Missing block: B:23:0x008f, code:
            r3 = r19.mService.mRoot.getDisplayContentOrCreate(r19.mDisplayContentsAnimators.keyAt(r9));
            r3.stepAppWindowsAnimation(r19.mCurrentTime);
            r4 = (com.android.server.wm.WindowAnimator.DisplayContentsAnimator) r19.mDisplayContentsAnimators.valueAt(r9);
            r11 = r4.mScreenRotationAnimation;
     */
    /* JADX WARNING: Missing block: B:24:0x00b4, code:
            if (r11 == null) goto L_0x00cc;
     */
    /* JADX WARNING: Missing block: B:26:0x00ba, code:
            if (r11.isAnimating() == false) goto L_0x00cc;
     */
    /* JADX WARNING: Missing block: B:28:0x00c4, code:
            if (r11.stepAnimationLocked(r19.mCurrentTime) == false) goto L_0x00ec;
     */
    /* JADX WARNING: Missing block: B:29:0x00c6, code:
            setAnimating(true);
     */
    /* JADX WARNING: Missing block: B:30:0x00cc, code:
            r19.mAnimTransactionSequence++;
            r3.updateWindowsForAnimator(r19);
            r3.updateWallpaperForAnimator(r19);
            r3.prepareWindowSurfaces();
     */
    /* JADX WARNING: Missing block: B:31:0x00e3, code:
            r9 = r9 + 1;
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            r19.mBulkUpdateParams |= 1;
            r11.kill();
            r4.mScreenRotationAnimation = null;
     */
    /* JADX WARNING: Missing block: B:36:0x00fc, code:
            if (r2 == null) goto L_0x00cc;
     */
    /* JADX WARNING: Missing block: B:38:0x0100, code:
            if (r3.isDefaultDisplay == false) goto L_0x00cc;
     */
    /* JADX WARNING: Missing block: B:39:0x0102, code:
            r2.onRotationChangedLocked(r19.mService.getDefaultDisplayContentLocked());
     */
    /* JADX WARNING: Missing block: B:40:0x010e, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:42:?, code:
            android.util.Slog.wtf(TAG, "Unhandled exception in Window Manager", r7);
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            r19.mService.closeSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:45:0x0120, code:
            if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS != false) goto L_0x0122;
     */
    /* JADX WARNING: Missing block: B:46:0x0122, code:
            android.util.Slog.i(TAG, "<<< CLOSE TRANSACTION animate");
     */
    /* JADX WARNING: Missing block: B:47:0x012a, code:
            r8 = r19.mService.mRoot.hasPendingLayoutChanges(r19);
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:48:0x013b, code:
            if (r19.mBulkUpdateParams == 0) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:49:0x013d, code:
            r6 = r19.mService.mRoot.copyAnimToLayoutParams();
     */
    /* JADX WARNING: Missing block: B:50:0x0147, code:
            if (r8 != false) goto L_0x014b;
     */
    /* JADX WARNING: Missing block: B:51:0x0149, code:
            if (r6 == false) goto L_0x0152;
     */
    /* JADX WARNING: Missing block: B:52:0x014b, code:
            r19.mWindowPlacerLocked.requestTraversal();
     */
    /* JADX WARNING: Missing block: B:54:0x0156, code:
            if (r19.mAnimating == false) goto L_0x0176;
     */
    /* JADX WARNING: Missing block: B:56:0x015e, code:
            if ((r19.mLastAnimating ^ 1) == 0) goto L_0x0176;
     */
    /* JADX WARNING: Missing block: B:57:0x0160, code:
            r19.mService.mTaskSnapshotController.setPersisterPaused(true);
            android.os.Trace.asyncTraceBegin(32, "animating", 0);
     */
    /* JADX WARNING: Missing block: B:59:0x017a, code:
            if (r19.mAnimating != false) goto L_0x019f;
     */
    /* JADX WARNING: Missing block: B:61:0x0180, code:
            if (r19.mLastAnimating == false) goto L_0x019f;
     */
    /* JADX WARNING: Missing block: B:62:0x0182, code:
            r19.mWindowPlacerLocked.requestTraversal();
            r19.mService.mTaskSnapshotController.setPersisterPaused(false);
            android.os.Trace.asyncTraceEnd(32, "animating", 0);
     */
    /* JADX WARNING: Missing block: B:63:0x019f, code:
            r19.mLastAnimating = r19.mAnimating;
     */
    /* JADX WARNING: Missing block: B:64:0x01ab, code:
            if (r19.mRemoveReplacedWindows == false) goto L_0x01bb;
     */
    /* JADX WARNING: Missing block: B:65:0x01ad, code:
            r19.mService.mRoot.removeReplacedWindows();
            r19.mRemoveReplacedWindows = false;
     */
    /* JADX WARNING: Missing block: B:66:0x01bb, code:
            r19.mService.stopUsingSavedSurfaceLocked();
            r19.mService.destroyPreservedSurfaceLocked();
            r19.mService.mWindowPlacerLocked.destroyPendingSurfaces();
     */
    /* JADX WARNING: Missing block: B:67:0x01d4, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE == false) goto L_0x021c;
     */
    /* JADX WARNING: Missing block: B:68:0x01d6, code:
            android.util.Slog.i(TAG, "!!! animate: exit mAnimating=" + r19.mAnimating + " mBulkUpdateParams=" + java.lang.Integer.toHexString(r19.mBulkUpdateParams) + " mPendingLayoutChanges(DEFAULT_DISPLAY)=" + java.lang.Integer.toHexString(getPendingLayoutChanges(0)));
     */
    /* JADX WARNING: Missing block: B:69:0x021c, code:
            monitor-exit(r13);
     */
    /* JADX WARNING: Missing block: B:70:0x021d, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:71:0x0220, code:
            return;
     */
    /* JADX WARNING: Missing block: B:72:0x0221, code:
            r9 = 0;
     */
    /* JADX WARNING: Missing block: B:73:0x0222, code:
            if (r9 >= r10) goto L_0x029a;
     */
    /* JADX WARNING: Missing block: B:75:?, code:
            r3 = r19.mService.mRoot.getDisplayContentOrCreate(r19.mDisplayContentsAnimators.keyAt(r9));
            r3.checkAppWindowsReadyToShow();
            r11 = ((com.android.server.wm.WindowAnimator.DisplayContentsAnimator) r19.mDisplayContentsAnimators.valueAt(r9)).mScreenRotationAnimation;
     */
    /* JADX WARNING: Missing block: B:76:0x0245, code:
            if (r11 == null) goto L_0x024a;
     */
    /* JADX WARNING: Missing block: B:77:0x0247, code:
            r11.updateSurfacesInTransaction();
     */
    /* JADX WARNING: Missing block: B:78:0x024a, code:
            orAnimating(r3.animateDimLayers());
            orAnimating(r3.getDockedDividerController().animate(r19.mCurrentTime));
     */
    /* JADX WARNING: Missing block: B:79:0x0264, code:
            if (r2 == null) goto L_0x0297;
     */
    /* JADX WARNING: Missing block: B:81:0x0268, code:
            if (r3.isDefaultDisplay == false) goto L_0x0297;
     */
    /* JADX WARNING: Missing block: B:83:0x0270, code:
            if (r19.mService.mDisplayMagnificationEnabled != false) goto L_0x0282;
     */
    /* JADX WARNING: Missing block: B:85:0x0278, code:
            if (r19.mService.mDisplayMagnificationEnabled != false) goto L_0x0297;
     */
    /* JADX WARNING: Missing block: B:87:0x0280, code:
            if (r19.mService.mMagnificationBorderDisappearCnt <= 0) goto L_0x0297;
     */
    /* JADX WARNING: Missing block: B:89:0x0288, code:
            if (r19.mService.mMagnificationBorderDisappearCnt <= 0) goto L_0x0294;
     */
    /* JADX WARNING: Missing block: B:90:0x028a, code:
            r12 = r19.mService;
            r12.mMagnificationBorderDisappearCnt--;
     */
    /* JADX WARNING: Missing block: B:91:0x0294, code:
            r2.drawMagnifiedRegionBorderIfNeededLocked();
     */
    /* JADX WARNING: Missing block: B:92:0x0297, code:
            r9 = r9 + 1;
     */
    /* JADX WARNING: Missing block: B:94:0x02a0, code:
            if (r19.mService.mDragState == null) goto L_0x02bd;
     */
    /* JADX WARNING: Missing block: B:95:0x02a2, code:
            r19.mAnimating |= r19.mService.mDragState.stepAnimationLocked(r19.mCurrentTime);
     */
    /* JADX WARNING: Missing block: B:97:0x02c1, code:
            if (r19.mAnimating != false) goto L_0x02c6;
     */
    /* JADX WARNING: Missing block: B:98:0x02c3, code:
            cancelAnimation();
     */
    /* JADX WARNING: Missing block: B:100:0x02cc, code:
            if (r19.mService.mWatermark == null) goto L_0x02d7;
     */
    /* JADX WARNING: Missing block: B:101:0x02ce, code:
            r19.mService.mWatermark.drawIfNeeded();
     */
    /* JADX WARNING: Missing block: B:103:?, code:
            r19.mService.closeSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:104:0x02e0, code:
            if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS == false) goto L_0x012a;
     */
    /* JADX WARNING: Missing block: B:105:0x02e2, code:
            android.util.Slog.i(TAG, "<<< CLOSE TRANSACTION animate");
     */
    /* JADX WARNING: Missing block: B:107:0x02ee, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:110:?, code:
            r19.mService.closeSurfaceTransaction();
     */
    /* JADX WARNING: Missing block: B:111:0x02fc, code:
            if (com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS != false) goto L_0x02fe;
     */
    /* JADX WARNING: Missing block: B:112:0x02fe, code:
            android.util.Slog.i(TAG, "<<< CLOSE TRANSACTION animate");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void animate(long frameTimeNs) {
        synchronized (this.mService.mWindowMap) {
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
        if ((bulkUpdateParams & 2) != 0) {
            builder.append(" WALLPAPER_MAY_CHANGE");
        }
        if ((bulkUpdateParams & 4) != 0) {
            builder.append(" FORCE_HIDING_CHANGED");
        }
        if ((bulkUpdateParams & 8) != 0) {
            builder.append(" ORIENTATION_CHANGE_COMPLETE");
        }
        if ((bulkUpdateParams & 16) != 0) {
            builder.append(" TURN_ON_SCREEN");
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
            DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.valueAt(i);
            this.mService.mRoot.getDisplayContentOrCreate(this.mDisplayContentsAnimators.keyAt(i)).dumpWindowAnimators(pw, subPrefix);
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
            pw.print("mAnimTransactionSequence=");
            pw.print(this.mAnimTransactionSequence);
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
        if (this.mWindowDetachedWallpaper != null) {
            pw.print(prefix);
            pw.print("mWindowDetachedWallpaper=");
            pw.println(this.mWindowDetachedWallpaper);
        }
    }

    int getPendingLayoutChanges(int displayId) {
        int i = 0;
        if (displayId < 0) {
            return 0;
        }
        DisplayContent displayContent = this.mService.mRoot.getDisplayContentOrCreate(displayId);
        if (displayContent != null) {
            i = displayContent.pendingLayoutChanges;
        }
        return i;
    }

    void setPendingLayoutChanges(int displayId, int changes) {
        if (displayId >= 0) {
            DisplayContent displayContent = this.mService.mRoot.getDisplayContentOrCreate(displayId);
            if (displayContent != null) {
                displayContent.pendingLayoutChanges |= changes;
            }
        }
    }

    private DisplayContentsAnimator getDisplayContentsAnimatorLocked(int displayId) {
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator displayAnimator = (DisplayContentsAnimator) this.mDisplayContentsAnimators.get(displayId);
        if (displayAnimator == null && this.mService.mRoot.getDisplayContent(displayId) != null) {
            displayAnimator = new DisplayContentsAnimator(this, null);
            this.mDisplayContentsAnimators.put(displayId, displayAnimator);
        }
        return displayAnimator;
    }

    void setScreenRotationAnimationLocked(int displayId, ScreenRotationAnimation animation) {
        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        if (animator != null) {
            animator.mScreenRotationAnimation = animation;
        }
    }

    ScreenRotationAnimation getScreenRotationAnimationLocked(int displayId) {
        ScreenRotationAnimation screenRotationAnimation = null;
        if (displayId < 0) {
            return null;
        }
        DisplayContentsAnimator animator = getDisplayContentsAnimatorLocked(displayId);
        if (animator != null) {
            screenRotationAnimation = animator.mScreenRotationAnimation;
        }
        return screenRotationAnimation;
    }

    void requestRemovalOfReplacedWindows(WindowState win) {
        this.mRemoveReplacedWindows = true;
    }

    void scheduleAnimation() {
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

    boolean isAnimating() {
        return this.mAnimating;
    }

    boolean isAnimationScheduled() {
        return this.mAnimationFrameCallbackScheduled;
    }

    Choreographer getChoreographer() {
        return this.mChoreographer;
    }

    void setAnimating(boolean animating) {
        this.mAnimating = animating;
    }

    void orAnimating(boolean animating) {
        this.mAnimating |= animating;
    }
}

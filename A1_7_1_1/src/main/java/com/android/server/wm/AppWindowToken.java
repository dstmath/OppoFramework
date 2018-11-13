package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Slog;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.View;
import android.view.animation.Animation;
import com.android.server.input.InputApplicationHandle;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;

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
class AppWindowToken extends WindowToken {
    private static final String TAG = null;
    final WindowList allAppWindows;
    boolean allDrawn;
    boolean allDrawnExcludingSaved;
    boolean appFullscreen;
    final IApplicationToken appToken;
    boolean clientHidden;
    boolean deferClearAllDrawn;
    boolean firstWindowDrawn;
    boolean hiddenRequested;
    boolean inPendingTransaction;
    long inputDispatchingTimeoutNanos;
    long lastTransactionSequence;
    boolean layoutConfigChanges;
    boolean mAlwaysFocusable;
    final AppWindowAnimator mAppAnimator;
    boolean mAppStopped;
    boolean mEnteringAnimation;
    ArrayDeque<Rect> mFrozenBounds;
    ArrayDeque<Configuration> mFrozenMergedConfig;
    final InputApplicationHandle mInputApplicationHandle;
    boolean mIsAboveInputMethod;
    boolean mIsExiting;
    boolean mLaunchTaskBehind;
    int mPendingRelaunchCount;
    int mRotationAnimationHint;
    boolean mSplitStoped;
    private ArrayList<SurfaceControlWithBackground> mSurfaceViewBackgrounds;
    Task mTask;
    boolean mVisitBeforeInputMethod;
    int numDrawnWindows;
    int numDrawnWindowsExclusingSaved;
    int numInterestingWindows;
    int numInterestingWindowsExcludingSaved;
    boolean removed;
    boolean reportedDrawn;
    boolean reportedVisible;
    int requestedOrientation;
    boolean showForAllUsers;
    StartingData startingData;
    boolean startingDisplayed;
    boolean startingMoved;
    View startingView;
    WindowState startingWindow;
    int targetSdk;
    final boolean voiceInteraction;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.AppWindowToken.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.AppWindowToken.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AppWindowToken.<clinit>():void");
    }

    AppWindowToken(WindowManagerService _service, IApplicationToken _token, boolean _voiceInteraction) {
        super(_service, _token.asBinder(), 2, true);
        this.allAppWindows = new WindowList();
        this.requestedOrientation = -1;
        this.lastTransactionSequence = Long.MIN_VALUE;
        this.mVisitBeforeInputMethod = false;
        this.mSurfaceViewBackgrounds = new ArrayList();
        this.mFrozenBounds = new ArrayDeque();
        this.mFrozenMergedConfig = new ArrayDeque();
        this.appWindowToken = this;
        this.appToken = _token;
        this.voiceInteraction = _voiceInteraction;
        this.mInputApplicationHandle = new InputApplicationHandle(this);
        this.mAppAnimator = new AppWindowAnimator(this);
    }

    void sendAppVisibilityToClients() {
        int N = this.allAppWindows.size();
        for (int i = 0; i < N; i++) {
            WindowState win = (WindowState) this.allAppWindows.get(i);
            if (win != this.startingWindow || !this.clientHidden) {
                try {
                    boolean z;
                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                        String str = TAG;
                        StringBuilder append = new StringBuilder().append("Setting visibility of ").append(win).append(": ");
                        if (this.clientHidden) {
                            z = false;
                        } else {
                            z = true;
                        }
                        Slog.v(str, append.append(z).toString());
                    }
                    IWindow iWindow = win.mClient;
                    if (this.clientHidden) {
                        z = false;
                    } else {
                        z = true;
                    }
                    iWindow.dispatchAppVisibility(z);
                } catch (RemoteException e) {
                }
            }
        }
    }

    void setVisibleBeforeClientHidden() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).setVisibleBeforeClientHidden();
        }
    }

    void onFirstWindowDrawn(WindowState win, WindowStateAnimator winAnimator) {
        this.firstWindowDrawn = true;
        removeAllDeadWindows();
        if (this.startingData != null) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "Finish starting " + win.mToken + ": first real window is shown, no animation");
            }
            winAnimator.clearAnimation();
            winAnimator.mService.mFinishedStarting.add(this);
            winAnimator.mService.mH.sendEmptyMessage(7);
        }
        updateReportedVisibilityLocked();
    }

    void updateReportedVisibilityLocked() {
        int i = 0;
        if (this.appToken != null) {
            int numInteresting = 0;
            int numVisible = 0;
            int numDrawn = 0;
            boolean nowGone = true;
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Update reported visibility: " + this);
            }
            int N = this.allAppWindows.size();
            for (int i2 = 0; i2 < N; i2++) {
                WindowState win = (WindowState) this.allAppWindows.get(i2);
                if (!(win == this.startingWindow || win.mAppFreezing || win.mViewVisibility != 0 || win.mAttrs.type == 3 || win.mDestroying)) {
                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG, "Win " + win + ": isDrawn=" + win.isDrawnLw() + ", isAnimationSet=" + win.mWinAnimator.isAnimationSet());
                        if (!win.isDrawnLw()) {
                            boolean z;
                            String str = TAG;
                            StringBuilder append = new StringBuilder().append("Not displayed: s=").append(win.mWinAnimator.mSurfaceController).append(" pv=").append(win.mPolicyVisibility).append(" mDrawState=").append(win.mWinAnimator.mDrawState).append(" ah=").append(win.mAttachedHidden).append(" th=");
                            if (win.mAppToken != null) {
                                z = win.mAppToken.hiddenRequested;
                            } else {
                                z = false;
                            }
                            Slog.v(str, append.append(z).append(" a=").append(win.mWinAnimator.mAnimating).toString());
                        }
                    }
                    numInteresting++;
                    if (win.isDrawnLw()) {
                        numDrawn++;
                        if (!win.mWinAnimator.isAnimationSet()) {
                            numVisible++;
                        }
                        nowGone = false;
                    } else if (win.mWinAnimator.isAnimationSet()) {
                        nowGone = false;
                    }
                }
            }
            boolean nowDrawn = numInteresting > 0 && numDrawn >= numInteresting;
            boolean nowVisible = numInteresting > 0 && numVisible >= numInteresting;
            if (!nowGone) {
                if (!nowDrawn) {
                    nowDrawn = this.reportedDrawn;
                }
                if (!nowVisible) {
                    nowVisible = this.reportedVisible;
                }
            }
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "VIS " + this + ": interesting=" + numInteresting + " visible=" + numVisible);
            }
            if (nowDrawn != this.reportedDrawn) {
                if (nowDrawn) {
                    this.service.mH.sendMessage(this.service.mH.obtainMessage(9, this));
                }
                this.reportedDrawn = nowDrawn;
            }
            if (nowVisible != this.reportedVisible) {
                int i3;
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG, "Visibility changed in " + this + ": vis=" + nowVisible);
                }
                this.reportedVisible = nowVisible;
                H h = this.service.mH;
                if (nowVisible) {
                    i3 = 1;
                } else {
                    i3 = 0;
                }
                if (nowGone) {
                    i = 1;
                }
                this.service.mH.sendMessage(h.obtainMessage(8, i3, i, this));
            }
        }
    }

    WindowState findMainWindow() {
        WindowState candidate = null;
        int j = this.windows.size();
        while (j > 0) {
            j--;
            WindowState win = (WindowState) this.windows.get(j);
            if (win.mAttrs.type == 1 || win.mAttrs.type == 3) {
                if (!win.mAnimatingExit) {
                    return win;
                }
                candidate = win;
            }
        }
        return candidate;
    }

    boolean windowsAreFocusable() {
        return !StackId.canReceiveKeys(this.mTask.mStack.mStackId) ? this.mAlwaysFocusable : true;
    }

    boolean isVisible() {
        int N = this.allAppWindows.size();
        for (int i = 0; i < N; i++) {
            WindowState win = (WindowState) this.allAppWindows.get(i);
            if (!win.mAppFreezing && ((win.mViewVisibility == 0 || win.isAnimatingWithSavedSurface() || (win.mWinAnimator.isAnimationSet() && !this.service.mAppTransition.isTransitionSet())) && !win.mDestroying && win.isDrawnLw())) {
                return true;
            }
        }
        return false;
    }

    void removeAppFromTaskLocked() {
        this.mIsExiting = false;
        removeAllWindows();
        Task task = this.mTask;
        if (task != null) {
            if (!task.removeAppToken(this)) {
                Slog.e(TAG, "removeAppFromTaskLocked: token=" + this + " not found.");
            }
            task.mStack.mExitingAppTokens.remove(this);
        }
    }

    void clearAnimatingFlags() {
        boolean wallpaperMightChange = false;
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.allAppWindows.get(i);
            if (!(win.mWillReplaceWindow || win.mRemoveOnExit)) {
                if (win.mAnimatingExit) {
                    win.mAnimatingExit = false;
                    wallpaperMightChange = true;
                }
                if (win.mWinAnimator.mAnimating) {
                    win.mWinAnimator.mAnimating = false;
                    wallpaperMightChange = true;
                }
                if (win.mDestroying) {
                    win.mDestroying = false;
                    this.service.mDestroySurface.remove(win);
                    wallpaperMightChange = true;
                }
            }
        }
        if (wallpaperMightChange) {
            requestUpdateWallpaperIfNeeded();
        }
    }

    void destroySurfaces() {
        destroySurfaces(false);
    }

    private void destroySurfaces(boolean cleanupOnResume) {
        int i;
        DisplayContent displayContent;
        ArrayList<WindowState> allWindows = (ArrayList) this.allAppWindows.clone();
        DisplayContentList displayList = new DisplayContentList();
        for (i = allWindows.size() - 1; i >= 0; i--) {
            boolean z;
            WindowState win = (WindowState) allWindows.get(i);
            if (this.mAppStopped || win.mWindowRemovalAllowed) {
                z = true;
            } else {
                z = cleanupOnResume;
            }
            if (z) {
                win.mWinAnimator.destroyPreservedSurfaceLocked(cleanupOnResume);
                if (win.mDestroying) {
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.e("WindowManager", "win=" + win + " destroySurfaces: mAppStopped=" + this.mAppStopped + " win.mWindowRemovalAllowed=" + win.mWindowRemovalAllowed + " win.mRemoveOnExit=" + win.mRemoveOnExit);
                    }
                    if (!cleanupOnResume || win.mRemoveOnExit) {
                        win.destroyOrSaveSurface();
                    }
                    if (win.mRemoveOnExit) {
                        this.service.removeWindowInnerLocked(win);
                    }
                    displayContent = win.getDisplayContent();
                    if (!(displayContent == null || displayList.contains(displayContent))) {
                        displayList.add(displayContent);
                    }
                    if (cleanupOnResume) {
                        win.requestUpdateWallpaperIfNeeded();
                    }
                    win.mDestroying = false;
                }
            }
        }
        for (i = 0; i < displayList.size(); i++) {
            displayContent = (DisplayContent) displayList.get(i);
            this.service.mLayersController.assignLayersLocked(displayContent.getWindowList());
            displayContent.layoutNeeded = true;
        }
    }

    void notifyAppResumed(boolean wasStopped, boolean allowSavedSurface) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v(TAG, "notifyAppResumed: wasStopped=" + wasStopped + " allowSavedSurface=" + allowSavedSurface + " " + this);
        }
        this.mAppStopped = false;
        this.mSplitStoped = false;
        if (!wasStopped) {
            destroySurfaces(true);
        }
        if (!allowSavedSurface) {
            destroySavedSurfaces();
        }
    }

    void notifyAppStopped() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v(TAG, "notifyAppStopped: " + this);
        }
        this.mAppStopped = true;
        this.mSplitStoped = true;
        destroySurfaces();
        this.mTask.mService.scheduleRemoveStartingWindowLocked(this);
    }

    boolean shouldSaveSurface() {
        return this.allDrawn;
    }

    boolean canRestoreSurfaces() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (((WindowState) this.allAppWindows.get(i)).canRestoreSurface()) {
                return true;
            }
        }
        return false;
    }

    void clearVisibleBeforeClientHidden() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).clearVisibleBeforeClientHidden();
        }
    }

    boolean isAnimatingInvisibleWithSavedSurface() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (((WindowState) this.allAppWindows.get(i)).isAnimatingInvisibleWithSavedSurface()) {
                return true;
            }
        }
        return false;
    }

    void stopUsingSavedSurfaceLocked() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) this.allAppWindows.get(i);
            if (w.isAnimatingInvisibleWithSavedSurface()) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                    Slog.d(TAG, "stopUsingSavedSurfaceLocked: " + w);
                }
                w.clearAnimatingWithSavedSurface();
                w.mDestroying = true;
                w.mWinAnimator.hide("stopUsingSavedSurfaceLocked");
                w.mWinAnimator.mWallpaperControllerLocked.hideWallpapers(w);
            }
        }
        destroySurfaces();
    }

    void markSavedSurfaceExiting() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) this.allAppWindows.get(i);
            if (w.isAnimatingInvisibleWithSavedSurface()) {
                w.mAnimatingExit = true;
                w.mWinAnimator.mAnimating = true;
            }
        }
    }

    void restoreSavedSurfaces() {
        boolean z = false;
        if (canRestoreSurfaces()) {
            int numInteresting = 0;
            int numDrawn = 0;
            for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
                WindowState w = (WindowState) this.allAppWindows.get(i);
                if (!(w == this.startingWindow || w.mAppDied || !w.wasVisibleBeforeClientHidden() || (this.mAppAnimator.freezingScreen && w.mAppFreezing))) {
                    numInteresting++;
                    if (w.hasSavedSurface()) {
                        w.restoreSavedSurface();
                    }
                    if (w.isDrawnLw()) {
                        numDrawn++;
                    }
                }
            }
            if (!this.allDrawn) {
                if (numInteresting > 0 && numInteresting == numDrawn) {
                    z = true;
                }
                this.allDrawn = z;
                if (this.allDrawn) {
                    this.service.mH.obtainMessage(32, this.token).sendToTarget();
                }
            }
            clearVisibleBeforeClientHidden();
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.d(TAG, "restoreSavedSurfaces: " + this.appWindowToken + " allDrawn=" + this.allDrawn + " numInteresting=" + numInteresting + " numDrawn=" + numDrawn);
            }
            return;
        }
        clearVisibleBeforeClientHidden();
    }

    void destroySavedSurfaces() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).destroySavedSurface();
        }
    }

    void clearAllDrawn() {
        this.allDrawn = false;
        this.deferClearAllDrawn = false;
        this.allDrawnExcludingSaved = false;
    }

    void removeAllWindows() {
        int winNdx = this.allAppWindows.size() - 1;
        while (winNdx >= 0) {
            WindowState win = (WindowState) this.allAppWindows.get(winNdx);
            if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                Slog.w(TAG, "removeAllWindows: removing win=" + win);
            }
            if (this.service.mH.hasMessages(46)) {
                win.mWillReplaceWindow = false;
                win.mReplacingWindow = null;
            }
            this.service.removeWindowLocked(win);
            if (this.allAppWindows.size() == 0) {
                break;
            }
            winNdx = Math.min(winNdx - 1, this.allAppWindows.size() - 1);
        }
        this.allAppWindows.clear();
        this.windows.clear();
    }

    void removeAllDeadWindows() {
        int winNdx = this.allAppWindows.size() - 1;
        while (winNdx >= 0) {
            WindowState win = (WindowState) this.allAppWindows.get(winNdx);
            if (win.mAppDied) {
                if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.w(TAG, "removeAllDeadWindows: " + win);
                }
                win.mDestroying = true;
                this.service.removeWindowLocked(win);
            }
            winNdx = Math.min(winNdx - 1, this.allAppWindows.size() - 1);
        }
    }

    boolean hasWindowsAlive() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (!((WindowState) this.allAppWindows.get(i)).mAppDied) {
                return true;
            }
        }
        return false;
    }

    void setReplacingWindows(boolean animate) {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Marking app token " + this.appWindowToken + " with replacing windows.");
        }
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).setReplacing(animate);
        }
        if (animate) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v("WindowManager", "setReplacingWindow() Setting dummy animation on: " + this);
            }
            this.mAppAnimator.setDummyAnimation();
        }
    }

    void setReplacingChildren() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Marking app token " + this.appWindowToken + " with replacing child windows.");
        }
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) this.allAppWindows.get(i);
            if (w.shouldBeReplacedWithChildren()) {
                w.setReplacing(false);
            }
        }
    }

    void resetReplacingWindows() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Resetting app token " + this.appWindowToken + " of replacing window marks.");
        }
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).resetReplacing();
        }
    }

    void requestUpdateWallpaperIfNeeded() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    boolean isRelaunching() {
        return this.mPendingRelaunchCount > 0;
    }

    void startRelaunching() {
        if (canFreezeBounds()) {
            freezeBounds();
        }
        this.mPendingRelaunchCount++;
    }

    void finishRelaunching() {
        if (canFreezeBounds()) {
            unfreezeBounds();
        }
        if (this.mPendingRelaunchCount > 0) {
            this.mPendingRelaunchCount--;
        }
    }

    void clearRelaunching() {
        if (this.mPendingRelaunchCount != 0) {
            if (canFreezeBounds()) {
                unfreezeBounds();
            }
            this.mPendingRelaunchCount = 0;
        }
    }

    void addWindow(WindowState w) {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState candidate = (WindowState) this.allAppWindows.get(i);
            if (candidate.mWillReplaceWindow && candidate.mReplacingWindow == null && candidate.getWindowTag().toString().equals(w.getWindowTag().toString())) {
                candidate.mReplacingWindow = w;
                w.mSkipEnterAnimationForSeamlessReplacement = !candidate.mAnimateReplacingWindow;
                this.service.scheduleReplacingWindowTimeouts(this);
            }
        }
        this.allAppWindows.add(w);
    }

    boolean waitingForReplacement() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (((WindowState) this.allAppWindows.get(i)).mWillReplaceWindow) {
                return true;
            }
        }
        return false;
    }

    void clearTimedoutReplacesLocked() {
        int i = this.allAppWindows.size() - 1;
        while (i >= 0) {
            WindowState candidate = (WindowState) this.allAppWindows.get(i);
            if (candidate.mWillReplaceWindow) {
                candidate.mWillReplaceWindow = false;
                if (candidate.mReplacingWindow != null) {
                    candidate.mReplacingWindow.mSkipEnterAnimationForSeamlessReplacement = false;
                }
                this.service.removeWindowInnerLocked(candidate);
            }
            i = Math.min(i - 1, this.allAppWindows.size() - 1);
        }
    }

    private boolean canFreezeBounds() {
        return (this.mTask == null || this.mTask.inFreeformWorkspace()) ? false : true;
    }

    private void freezeBounds() {
        this.mFrozenBounds.offer(new Rect(this.mTask.mPreparedFrozenBounds));
        if (this.mTask.mPreparedFrozenMergedConfig.equals(Configuration.EMPTY)) {
            Configuration config = new Configuration(this.service.mCurConfiguration);
            config.updateFrom(this.mTask.mOverrideConfig);
            this.mFrozenMergedConfig.offer(config);
        } else {
            this.mFrozenMergedConfig.offer(new Configuration(this.mTask.mPreparedFrozenMergedConfig));
        }
        this.mTask.mPreparedFrozenMergedConfig.setToDefaults();
    }

    private void unfreezeBounds() {
        if (!this.mFrozenBounds.isEmpty()) {
            this.mFrozenBounds.remove();
        }
        if (!this.mFrozenMergedConfig.isEmpty()) {
            this.mFrozenMergedConfig.remove();
        }
        for (int i = this.windows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.windows.get(i);
            if (win.mHasSurface) {
                win.mLayoutNeeded = true;
                win.setDisplayLayoutNeeded();
                if (!this.service.mResizingWindows.contains(win)) {
                    this.service.mResizingWindows.add(win);
                }
            }
        }
        this.service.mWindowPlacerLocked.performSurfacePlacement();
    }

    void addSurfaceViewBackground(SurfaceControlWithBackground background) {
        this.mSurfaceViewBackgrounds.add(background);
    }

    void removeSurfaceViewBackground(SurfaceControlWithBackground background) {
        this.mSurfaceViewBackgrounds.remove(background);
        updateSurfaceViewBackgroundVisibilities();
    }

    void updateSurfaceViewBackgroundVisibilities() {
        int i;
        SurfaceControlWithBackground sc;
        SurfaceControlWithBackground bottom = null;
        int bottomLayer = Integer.MAX_VALUE;
        for (i = 0; i < this.mSurfaceViewBackgrounds.size(); i++) {
            sc = (SurfaceControlWithBackground) this.mSurfaceViewBackgrounds.get(i);
            if (sc.mVisible && sc.mLayer < bottomLayer) {
                bottomLayer = sc.mLayer;
                bottom = sc;
            }
        }
        for (i = 0; i < this.mSurfaceViewBackgrounds.size(); i++) {
            sc = (SurfaceControlWithBackground) this.mSurfaceViewBackgrounds.get(i);
            sc.updateBackgroundVisibility(sc != bottom);
        }
    }

    void overridePlayingAppAnimations(Animation a) {
        if (this.mAppAnimator.isAnimating()) {
            WindowState win = findMainWindow();
            if (win != null) {
                this.mAppAnimator.setAnimation(a, win.mContainingFrame.width(), win.mContainingFrame.height(), false, 2);
            }
        }
    }

    void dump(PrintWriter pw, String prefix) {
        super.dump(pw, prefix);
        if (this.appToken != null) {
            pw.print(prefix);
            pw.print("app=true voiceInteraction=");
            pw.println(this.voiceInteraction);
        }
        if (this.allAppWindows.size() > 0) {
            pw.print(prefix);
            pw.print("allAppWindows=");
            pw.println(this.allAppWindows);
        }
        if (WindowManagerDebugConfig.DEBUG_STACK) {
            pw.print(prefix);
            pw.print("task=");
            pw.println(this.mTask);
        } else {
            pw.print(prefix);
            pw.print("taskId=");
            pw.println(this.mTask.mTaskId);
        }
        pw.print(prefix);
        pw.print(" appFullscreen=");
        pw.print(this.appFullscreen);
        pw.print(" requestedOrientation=");
        pw.println(this.requestedOrientation);
        pw.print(prefix);
        pw.print("hiddenRequested=");
        pw.print(this.hiddenRequested);
        pw.print(" clientHidden=");
        pw.print(this.clientHidden);
        pw.print(" reportedDrawn=");
        pw.print(this.reportedDrawn);
        pw.print(" reportedVisible=");
        pw.println(this.reportedVisible);
        if (this.paused) {
            pw.print(prefix);
            pw.print("paused=");
            pw.println(this.paused);
        }
        if (this.mAppStopped) {
            pw.print(prefix);
            pw.print("mAppStopped=");
            pw.println(this.mAppStopped);
        }
        if (this.numInterestingWindows != 0 || this.numDrawnWindows != 0 || this.allDrawn || this.mAppAnimator.allDrawn) {
            pw.print(prefix);
            pw.print("numInterestingWindows=");
            pw.print(this.numInterestingWindows);
            pw.print(" numDrawnWindows=");
            pw.print(this.numDrawnWindows);
            pw.print(" inPendingTransaction=");
            pw.print(this.inPendingTransaction);
            pw.print(" allDrawn=");
            pw.print(this.allDrawn);
            pw.print(" (animator=");
            pw.print(this.mAppAnimator.allDrawn);
            pw.println(")");
        }
        if (this.inPendingTransaction) {
            pw.print(prefix);
            pw.print("inPendingTransaction=");
            pw.println(this.inPendingTransaction);
        }
        if (this.startingData != null || this.removed || this.firstWindowDrawn || this.mIsExiting) {
            pw.print(prefix);
            pw.print("startingData=");
            pw.print(this.startingData);
            pw.print(" removed=");
            pw.print(this.removed);
            pw.print(" firstWindowDrawn=");
            pw.print(this.firstWindowDrawn);
            pw.print(" mIsExiting=");
            pw.println(this.mIsExiting);
        }
        if (this.startingWindow != null || this.startingView != null || this.startingDisplayed || this.startingMoved) {
            pw.print(prefix);
            pw.print("startingWindow=");
            pw.print(this.startingWindow);
            pw.print(" startingView=");
            pw.print(this.startingView);
            pw.print(" startingDisplayed=");
            pw.print(this.startingDisplayed);
            pw.print(" startingMoved=");
            pw.println(this.startingMoved);
        }
        if (!this.mFrozenBounds.isEmpty()) {
            pw.print(prefix);
            pw.print("mFrozenBounds=");
            pw.println(this.mFrozenBounds);
            pw.print(prefix);
            pw.print("mFrozenMergedConfig=");
            pw.println(this.mFrozenMergedConfig);
        }
        if (this.mPendingRelaunchCount != 0) {
            pw.print(prefix);
            pw.print("mPendingRelaunchCount=");
            pw.println(this.mPendingRelaunchCount);
        }
    }

    public String toString() {
        if (this.stringName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AppWindowToken{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" token=");
            sb.append(this.token);
            sb.append('}');
            this.stringName = sb.toString();
        }
        return this.stringName;
    }
}

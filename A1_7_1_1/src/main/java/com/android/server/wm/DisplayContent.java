package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.animation.Animation;
import com.android.server.display.OppoBrightUtils;
import com.mediatek.multiwindow.MultiWindowManager;
import java.io.PrintWriter;
import java.util.ArrayList;

class DisplayContent {
    final boolean isDefaultDisplay;
    boolean layoutNeeded;
    int mBaseDisplayDensity = 0;
    int mBaseDisplayHeight = 0;
    Rect mBaseDisplayRect = new Rect();
    int mBaseDisplayWidth = 0;
    Rect mContentRect = new Rect();
    boolean mDeferredRemoval;
    final DimLayerController mDimLayerController;
    private final Display mDisplay;
    private final int mDisplayId;
    private final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    boolean mDisplayScalingDisabled;
    final DockedStackDividerController mDividerControllerLocked;
    final ArrayList<WindowToken> mExitingTokens = new ArrayList();
    private TaskStack mHomeStack = null;
    int mInitialDisplayDensity = 0;
    int mInitialDisplayHeight = 0;
    int mInitialDisplayWidth = 0;
    Region mNonResizeableRegion = new Region();
    final WindowManagerService mService;
    private final ArrayList<TaskStack> mStacks = new ArrayList();
    TaskTapPointerEventListener mTapDetector;
    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList();
    private final Matrix mTmpMatrix = new Matrix();
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final RectF mTmpRectF = new RectF();
    private final Region mTmpRegion = new Region();
    final ArrayList<Task> mTmpTaskHistory = new ArrayList();
    Region mTouchExcludeRegion = new Region();
    private final WindowList mWindows = new WindowList();
    int pendingLayoutChanges;

    DisplayContent(Display display, WindowManagerService service) {
        boolean z = false;
        this.mDisplay = display;
        this.mDisplayId = display.getDisplayId();
        display.getDisplayInfo(this.mDisplayInfo);
        display.getMetrics(this.mDisplayMetrics);
        if (this.mDisplayId == 0) {
            z = true;
        }
        this.isDefaultDisplay = z;
        this.mService = service;
        initializeDisplayBaseInfo();
        this.mDividerControllerLocked = new DockedStackDividerController(service, this);
        this.mDimLayerController = new DimLayerController(this);
    }

    int getDisplayId() {
        return this.mDisplayId;
    }

    WindowList getWindowList() {
        return this.mWindows;
    }

    Display getDisplay() {
        return this.mDisplay;
    }

    DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    DockedStackDividerController getDockedDividerController() {
        return this.mDividerControllerLocked;
    }

    public boolean hasAccess(int uid) {
        return this.mDisplay.hasAccess(uid);
    }

    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    ArrayList<TaskStack> getStacks() {
        return this.mStacks;
    }

    ArrayList<Task> getTasks() {
        this.mTmpTaskHistory.clear();
        int numStacks = this.mStacks.size();
        for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
            this.mTmpTaskHistory.addAll(((TaskStack) this.mStacks.get(stackNdx)).getTasks());
        }
        return this.mTmpTaskHistory;
    }

    TaskStack getHomeStack() {
        if (this.mHomeStack == null && this.mDisplayId == 0) {
            Slog.e("WindowManager", "getHomeStack: Returning null from this=" + this);
        }
        return this.mHomeStack;
    }

    TaskStack getStackById(int stackId) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mStacks.get(i);
            if (stack.mStackId == stackId) {
                return stack;
            }
        }
        return null;
    }

    void updateDisplayInfo() {
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ((TaskStack) this.mStacks.get(i)).updateDisplayInfo(null);
        }
    }

    void initializeDisplayBaseInfo() {
        DisplayInfo newDisplayInfo = this.mService.mDisplayManagerInternal.getDisplayInfo(this.mDisplayId);
        if (newDisplayInfo != null) {
            this.mDisplayInfo.copyFrom(newDisplayInfo);
        }
        int i = this.mDisplayInfo.logicalWidth;
        this.mInitialDisplayWidth = i;
        this.mBaseDisplayWidth = i;
        i = this.mDisplayInfo.logicalHeight;
        this.mInitialDisplayHeight = i;
        this.mBaseDisplayHeight = i;
        i = this.mDisplayInfo.logicalDensityDpi;
        this.mInitialDisplayDensity = i;
        this.mBaseDisplayDensity = i;
        this.mBaseDisplayRect.set(0, 0, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
    }

    void getLogicalDisplayRect(Rect out) {
        boolean rotated = true;
        int orientation = this.mDisplayInfo.rotation;
        if (!(orientation == 1 || orientation == 3)) {
            rotated = false;
        }
        int physWidth = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int physHeight = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int width = this.mDisplayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = this.mDisplayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    private void getLogicalDisplayRect(Rect out, int orientation) {
        getLogicalDisplayRect(out);
        int rotationDelta = deltaRotation(this.mDisplayInfo.rotation, orientation);
        if (rotationDelta == 1 || rotationDelta == 3) {
            createRotationMatrix(rotationDelta, (float) this.mBaseDisplayWidth, (float) this.mBaseDisplayHeight, this.mTmpMatrix);
            this.mTmpRectF.set(out);
            this.mTmpMatrix.mapRect(this.mTmpRectF);
            this.mTmpRectF.round(out);
        }
    }

    void getContentRect(Rect out) {
        out.set(this.mContentRect);
    }

    void attachStack(TaskStack stack, boolean onTop) {
        if (stack.mStackId == 0) {
            if (this.mHomeStack != null) {
                throw new IllegalArgumentException("attachStack: HOME_STACK_ID (0) not first.");
            }
            this.mHomeStack = stack;
        }
        if (onTop) {
            this.mStacks.add(stack);
        } else {
            this.mStacks.add(0, stack);
        }
        this.layoutNeeded = true;
    }

    void moveStack(TaskStack stack, boolean toTop) {
        if (!StackId.isAlwaysOnTop(stack.mStackId) || toTop) {
            if (!this.mStacks.remove(stack)) {
                Slog.wtf("WindowManager", "moving stack that was not added: " + stack, new Throwable());
            }
            int addIndex = toTop ? this.mStacks.size() : 0;
            if (toTop && this.mService.isStackVisibleLocked(4) && stack.mStackId != 4) {
                addIndex--;
                if (((TaskStack) this.mStacks.get(addIndex)).mStackId != 4) {
                    throw new IllegalStateException("Pinned stack isn't top stack??? " + this.mStacks);
                }
            }
            this.mStacks.add(addIndex, stack);
            return;
        }
        Slog.w("WindowManager", "Ignoring move of always-on-top stack=" + stack + " to bottom");
    }

    void detachStack(TaskStack stack) {
        this.mDimLayerController.removeDimLayerUser(stack);
        this.mStacks.remove(stack);
    }

    void resize(Rect contentRect) {
        this.mContentRect.set(contentRect);
    }

    int taskIdFromPoint(int x, int y) {
        return taskIdFromPoint(x, y, false);
    }

    int taskIdFromPoint(int x, int y, boolean skipFullscreen) {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            stack.getBounds(this.mTmpRect);
            if (this.mTmpRect.contains(x, y) && !stack.isAdjustedForMinimizedDockedStack()) {
                ArrayList<Task> tasks = stack.getTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    Task task = (Task) tasks.get(taskNdx);
                    if (task.getTopVisibleAppMainWindow() != null) {
                        if (!skipFullscreen || !task.isFullscreen()) {
                            task.getDimBounds(this.mTmpRect);
                            if (this.mTmpRect.contains(x, y)) {
                                return task.mTaskId;
                            }
                        } else if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.d("WindowManager", "taskIdFromPoint skip fullscreen task:" + task);
                        }
                    }
                }
                continue;
            }
        }
        return -1;
    }

    Task findTaskForControlPoint(int x, int y) {
        WindowManagerService windowManagerService = this.mService;
        int delta = WindowManagerService.dipToPixel(30, this.mDisplayMetrics);
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            if (!StackId.isTaskResizeAllowed(stack.mStackId)) {
                break;
            }
            ArrayList<Task> tasks = stack.getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                Task task = (Task) tasks.get(taskNdx);
                if (task.isFullscreen()) {
                    return null;
                }
                task.getDimBounds(this.mTmpRect);
                this.mTmpRect.inset(-delta, -delta);
                if (this.mTmpRect.contains(x, y)) {
                    this.mTmpRect.inset(delta, delta);
                    if (this.mTmpRect.contains(x, y)) {
                        return null;
                    }
                    return task;
                }
            }
        }
        return null;
    }

    void setTouchExcludeRegion(Task focusedTask) {
        Task stickyTask = null;
        this.mTouchExcludeRegion.set(this.mBaseDisplayRect);
        WindowManagerService windowManagerService = this.mService;
        int delta = WindowManagerService.dipToPixel(30, this.mDisplayMetrics);
        boolean addBackFocusedTask = false;
        this.mNonResizeableRegion.setEmpty();
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            ArrayList<Task> tasks = stack.getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                Task task = (Task) tasks.get(taskNdx);
                if (MultiWindowManager.isSupported() && task.mSticky) {
                    stickyTask = task;
                }
                AppWindowToken token = task.getTopVisibleAppToken();
                if (token != null && token.isVisible()) {
                    task.getDimBounds(this.mTmpRect);
                    if (task == focusedTask) {
                        addBackFocusedTask = true;
                        this.mTmpRect2.set(this.mTmpRect);
                    }
                    boolean isFreeformed = task.inFreeformWorkspace();
                    if (task != focusedTask || isFreeformed) {
                        if (isFreeformed) {
                            this.mTmpRect.inset(-delta, -delta);
                            this.mTmpRect.intersect(this.mContentRect);
                        }
                        this.mTouchExcludeRegion.op(this.mTmpRect, Op.DIFFERENCE);
                    }
                    if (task.isTwoFingerScrollMode()) {
                        stack.getBounds(this.mTmpRect);
                        this.mNonResizeableRegion.op(this.mTmpRect, Op.UNION);
                        break;
                    }
                }
            }
        }
        if (addBackFocusedTask) {
            this.mTouchExcludeRegion.op(this.mTmpRect2, Op.UNION);
        }
        WindowState inputMethod = this.mService.mInputMethodWindow;
        if (inputMethod != null && inputMethod.isVisibleLw()) {
            inputMethod.getTouchableRegion(this.mTmpRegion);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
        }
        for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mTapExcludedWindows.get(i);
            if (win == null || win.mHasSurface || win.mAttrs == null || win.mAttrs.type != 2014) {
                win.getTouchableRegion(this.mTmpRegion);
                this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
            }
        }
        if (getDockedStackVisibleForUserLocked() != null) {
            this.mDividerControllerLocked.getTouchRegion(this.mTmpRect);
            this.mTmpRegion.set(this.mTmpRect);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
        }
        if (!(!MultiWindowManager.isSupported() || stickyTask == null || stickyTask == focusedTask)) {
            Rect rect = new Rect();
            stickyTask.getBounds(rect);
            Region tmpStickyRegion = new Region(rect);
            this.mTouchExcludeRegion.op(tmpStickyRegion, Op.DIFFERENCE);
        }
        if (this.mTapDetector != null) {
            this.mTapDetector.setTouchExcludeRegion(this.mTouchExcludeRegion, this.mNonResizeableRegion);
        }
    }

    void switchUserStacks() {
        WindowList windows = getWindowList();
        for (int i = 0; i < windows.size(); i++) {
            WindowState win = (WindowState) windows.get(i);
            if (win.isHiddenFromUserLocked()) {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.w("WindowManager", "user changing, hiding " + win + ", attrs=" + win.mAttrs.type + ", belonging to " + win.mOwnerUid);
                }
                win.hideLw(false);
            }
        }
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).switchUser();
        }
    }

    void resetAnimationBackgroundAnimator() {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).resetAnimationBackgroundAnimator();
        }
    }

    boolean animateDimLayers() {
        return this.mDimLayerController.animateDimLayers();
    }

    void resetDimming() {
        this.mDimLayerController.resetDimming();
    }

    boolean isDimming() {
        return this.mDimLayerController.isDimming();
    }

    void stopDimmingIfNeeded() {
        this.mDimLayerController.stopDimmingIfNeeded();
    }

    void close() {
        this.mDimLayerController.close();
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).close();
        }
    }

    boolean isAnimating() {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            if (((TaskStack) this.mStacks.get(stackNdx)).isAnimating()) {
                return true;
            }
        }
        return false;
    }

    void checkForDeferredActions() {
        boolean animating = false;
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            if (stack.isAnimating()) {
                animating = true;
            } else {
                if (stack.mDeferDetach) {
                    this.mService.detachStackLocked(this, stack);
                }
                ArrayList<Task> tasks = stack.getTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                    for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                        AppWindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                        if (wtoken.mIsExiting) {
                            if (wtoken.mTask != null) {
                                this.mDimLayerController.removeDimLayerUser(wtoken.mTask);
                            }
                            wtoken.removeAppFromTaskLocked();
                        }
                    }
                }
            }
        }
        if (!animating && this.mDeferredRemoval) {
            this.mService.onDisplayRemoved(this.mDisplayId);
        }
    }

    void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        getLogicalDisplayRect(this.mTmpRect, newRotation);
        createRotationMatrix(deltaRotation(newRotation, oldRotation), (float) this.mTmpRect.width(), (float) this.mTmpRect.height(), this.mTmpMatrix);
        this.mTmpRectF.set(bounds);
        this.mTmpMatrix.mapRect(this.mTmpRectF);
        this.mTmpRectF.round(bounds);
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            return delta + 4;
        }
        return delta;
    }

    static void createRotationMatrix(int rotation, float displayWidth, float displayHeight, Matrix outMatrix) {
        createRotationMatrix(rotation, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, displayWidth, displayHeight, outMatrix);
    }

    static void createRotationMatrix(int rotation, float rectLeft, float rectTop, float displayWidth, float displayHeight, Matrix outMatrix) {
        switch (rotation) {
            case 0:
                outMatrix.reset();
                return;
            case 1:
                outMatrix.setRotate(90.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                outMatrix.postTranslate(displayWidth, OppoBrightUtils.MIN_LUX_LIMITI);
                outMatrix.postTranslate(-rectTop, rectLeft);
                return;
            case 2:
                outMatrix.reset();
                return;
            case 3:
                outMatrix.setRotate(270.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                outMatrix.postTranslate(OppoBrightUtils.MIN_LUX_LIMITI, displayHeight);
                outMatrix.postTranslate(rectTop, OppoBrightUtils.MIN_LUX_LIMITI);
                return;
            default:
                return;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("Display: mDisplayId=");
        pw.println(this.mDisplayId);
        String subPrefix = "  " + prefix;
        pw.print(subPrefix);
        pw.print("init=");
        pw.print(this.mInitialDisplayWidth);
        pw.print("x");
        pw.print(this.mInitialDisplayHeight);
        pw.print(" ");
        pw.print(this.mInitialDisplayDensity);
        pw.print("dpi");
        if (!(this.mInitialDisplayWidth == this.mBaseDisplayWidth && this.mInitialDisplayHeight == this.mBaseDisplayHeight && this.mInitialDisplayDensity == this.mBaseDisplayDensity)) {
            pw.print(" base=");
            pw.print(this.mBaseDisplayWidth);
            pw.print("x");
            pw.print(this.mBaseDisplayHeight);
            pw.print(" ");
            pw.print(this.mBaseDisplayDensity);
            pw.print("dpi");
        }
        if (this.mDisplayScalingDisabled) {
            pw.println(" noscale");
        }
        pw.print(" cur=");
        pw.print(this.mDisplayInfo.logicalWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.logicalHeight);
        pw.print(" app=");
        pw.print(this.mDisplayInfo.appWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.appHeight);
        pw.print(" rng=");
        pw.print(this.mDisplayInfo.smallestNominalAppWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.smallestNominalAppHeight);
        pw.print("-");
        pw.print(this.mDisplayInfo.largestNominalAppWidth);
        pw.print("x");
        pw.println(this.mDisplayInfo.largestNominalAppHeight);
        pw.print(subPrefix);
        pw.print("deferred=");
        pw.print(this.mDeferredRemoval);
        pw.print(" layoutNeeded=");
        pw.println(this.layoutNeeded);
        pw.println();
        pw.println("  Application tokens in top down Z order:");
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).dump(prefix + "  ", pw);
        }
        pw.println();
        if (!this.mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) this.mExitingTokens.get(i);
                pw.print("  Exiting #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                pw.println(':');
                token.dump(pw, "    ");
            }
        }
        pw.println();
        this.mDimLayerController.dump(prefix + "  ", pw);
        pw.println();
        this.mDividerControllerLocked.dump(prefix + "  ", pw);
    }

    public String toString() {
        return "Display " + this.mDisplayId + " info=" + this.mDisplayInfo + " stacks=" + this.mStacks;
    }

    TaskStack getDockedStackLocked() {
        TaskStack stack = (TaskStack) this.mService.mStackIdToStack.get(3);
        return (stack == null || !stack.isVisibleLocked()) ? null : stack;
    }

    TaskStack getDockedStackVisibleForUserLocked() {
        TaskStack stack = (TaskStack) this.mService.mStackIdToStack.get(3);
        return (stack == null || !stack.isVisibleLocked(true)) ? null : stack;
    }

    WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        int x = (int) xf;
        int y = (int) yf;
        for (int i = this.mWindows.size() - 1; i >= 0; i--) {
            WindowState window = (WindowState) this.mWindows.get(i);
            int flags = window.mAttrs.flags;
            if (window.isVisibleLw() && (flags & 16) == 0) {
                window.getVisibleBounds(this.mTmpRect);
                if (this.mTmpRect.contains(x, y)) {
                    window.getTouchableRegion(this.mTmpRegion);
                    int touchFlags = flags & 40;
                    if (this.mTmpRegion.contains(x, y) || touchFlags == 0) {
                        return window;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    void overridePlayingAppAnimationsLw(Animation a) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ((TaskStack) this.mStacks.get(i)).overridePlayingAppAnimations(a);
        }
    }

    boolean canAddToastWindowForUid(int uid) {
        return canAddToastWindowForUid(uid, true);
    }

    boolean canAddToastWindowForUid(int uid, boolean force) {
        boolean z = true;
        if (1000 == uid) {
            return true;
        }
        int num = 0;
        boolean alreadyHasToastWindow = false;
        int windowCount = this.mWindows.size();
        for (int i = 0; i < windowCount; i++) {
            WindowState window = (WindowState) this.mWindows.get(i);
            if (window.isFocused() && window.getOwningUid() == uid) {
                return true;
            }
            if (window.mAttrs.type == 2005 && window.getOwningUid() == uid && !window.isRemovedOrHidden()) {
                Slog.w("WindowManager", "Found toast window " + window + ", attrs=" + window.mAttrs + " for uid: " + uid);
                if (force) {
                    alreadyHasToastWindow = true;
                } else {
                    num++;
                }
            }
        }
        if (num > 2) {
            alreadyHasToastWindow = true;
        }
        if (alreadyHasToastWindow) {
            z = false;
        }
        return z;
    }

    void scheduleToastWindowsTimeoutIfNeededLocked(WindowState oldFocus, WindowState newFocus) {
        if (oldFocus != null && (newFocus == null || newFocus.mOwnerUid != oldFocus.mOwnerUid)) {
            int lostFocusUid = oldFocus.mOwnerUid;
            WindowList windows = getWindowList();
            int windowCount = windows.size();
            for (int i = 0; i < windowCount; i++) {
                WindowState window = (WindowState) windows.get(i);
                if (window.mAttrs.type == 2005 && window.mOwnerUid == lostFocusUid && !this.mService.mH.hasMessages(52, window)) {
                    this.mService.mH.sendMessageDelayed(this.mService.mH.obtainMessage(52, window), window.mAttrs.hideTimeoutMilliseconds);
                }
            }
        }
    }
}

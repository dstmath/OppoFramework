package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.display.OppoBrightUtils;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.wm.LaunchParamsController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public class TaskLaunchParamsModifier implements LaunchParamsController.LaunchParamsModifier {
    private static final int BOUNDS_CONFLICT_THRESHOLD = 4;
    private static final int CASCADING_OFFSET_DP = 75;
    private static final boolean DEBUG = false;
    private static int DEFAULT_DENSITY = 3;
    private static int DEFAULT_FREEFORM_HEIGHT = 314;
    private static int DEFAULT_FREEFORM_WIDTH = 296;
    private static int DEFAULT_FREEFORM_X_0 = 14;
    private static int DEFAULT_FREEFORM_X_90 = 82;
    private static int DEFAULT_FREEFORM_Y_0 = 100;
    private static int DEFAULT_FREEFORM_Y_90 = 26;
    private static final int DEFAULT_PORTRAIT_PHONE_HEIGHT_DP = 732;
    private static final int DEFAULT_PORTRAIT_PHONE_WIDTH_DP = 412;
    private static final int EPSILON = 2;
    private static final int MINIMAL_STEP = 1;
    private static int SCREEN_HEIGHT = 2280;
    private static final int STEP_DENOMINATOR = 16;
    private static final int SUPPORTS_SCREEN_RESIZEABLE_MASK = 539136;
    private static final String TAG = "ActivityTaskManager";
    private static int mDensity = -1;
    private static int mDiviceLogicalDisplayHeight = 0;
    private StringBuilder mLogBuilder;
    private int mRotation;
    private final ActivityStackSupervisor mSupervisor;
    private final Rect mTmpBounds = new Rect();
    private final int[] mTmpDirections = new int[2];

    TaskLaunchParamsModifier(ActivityStackSupervisor supervisor) {
        this.mSupervisor = supervisor;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int onCalculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        return onCalculate(task, layout, activity, source, options, 2, currentParams, outParams);
    }

    @Override // com.android.server.wm.LaunchParamsController.LaunchParamsModifier
    public int onCalculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, int phase, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        initLogBuilder(task, activity);
        int result = calculate(task, layout, activity, source, options, phase, currentParams, outParams);
        outputLog();
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e9  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00eb  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00f1 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00f2  */
    private int calculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, int phase, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        ActivityRecord root;
        int launchMode;
        boolean hasInitialBounds;
        boolean fullyResolvedCurrentParam;
        int launchMode2;
        int resolvedMode;
        int i;
        if (task != null) {
            root = task.getRootActivity() == null ? activity : task.getRootActivity();
        } else {
            root = activity;
        }
        if (root == null) {
            return 0;
        }
        int displayId = getPreferredLaunchDisplay(task, options, source, currentParams);
        outParams.mPreferredDisplayId = displayId;
        ActivityDisplay display = this.mSupervisor.mRootActivityContainer.getActivityDisplay(displayId);
        if (phase == 0) {
            return 2;
        }
        if (options != null) {
            launchMode = options.getLaunchWindowingMode();
        } else {
            launchMode = 0;
        }
        boolean canApplyFreeformPolicy = canApplyFreeformWindowPolicy(display, launchMode);
        if (!this.mSupervisor.canUseActivityOptionsLaunchBounds(options) || (!canApplyFreeformPolicy && !canApplyPipWindowPolicy(launchMode))) {
            if (!(launchMode == 2 || launchMode == 1 || layout == null || !canApplyFreeformPolicy)) {
                getLayoutBounds(display, root, layout, this.mTmpBounds);
                if (!this.mTmpBounds.isEmpty()) {
                    launchMode = 5;
                    outParams.mBounds.set(this.mTmpBounds);
                    hasInitialBounds = true;
                }
            }
            hasInitialBounds = false;
        } else {
            if (launchMode == 0) {
                i = 5;
            } else {
                i = launchMode;
            }
            launchMode = i;
            outParams.mBounds.set(options.getLaunchBounds());
            hasInitialBounds = true;
        }
        boolean fullyResolvedCurrentParam2 = false;
        if (!currentParams.isEmpty() && !hasInitialBounds && (!currentParams.hasPreferredDisplay() || displayId == currentParams.mPreferredDisplayId)) {
            if (currentParams.hasWindowingMode()) {
                launchMode = currentParams.mWindowingMode;
                fullyResolvedCurrentParam2 = launchMode != 5;
            }
            if (!currentParams.mBounds.isEmpty()) {
                outParams.mBounds.set(currentParams.mBounds);
                if (launchMode == 5) {
                    fullyResolvedCurrentParam = true;
                    if (display.inFreeformWindowingMode() || launchMode == 2 || !isTaskForcedMaximized(root)) {
                        launchMode2 = launchMode;
                    } else {
                        outParams.mBounds.setEmpty();
                        launchMode2 = 1;
                    }
                    outParams.mWindowingMode = launchMode2 != display.getWindowingMode() ? 0 : launchMode2;
                    if (phase != 1) {
                        return 2;
                    }
                    if (launchMode2 != 0) {
                        resolvedMode = launchMode2;
                    } else {
                        resolvedMode = display.getWindowingMode();
                    }
                    if (!fullyResolvedCurrentParam) {
                        if (source != null && source.inFreeformWindowingMode() && resolvedMode == 5 && outParams.mBounds.isEmpty() && source.getDisplayId() == display.mDisplayId) {
                            cascadeBounds(source.getBounds(), display, outParams.mBounds);
                        }
                        getTaskBounds(root, display, layout, resolvedMode, hasInitialBounds, outParams.mBounds);
                        return 2;
                    } else if (resolvedMode != 5) {
                        return 2;
                    } else {
                        if (currentParams.mPreferredDisplayId != displayId) {
                            adjustBoundsToFitInDisplay(display, outParams.mBounds);
                        }
                        adjustBoundsToAvoidConflictInDisplay(display, outParams.mBounds);
                        return 2;
                    }
                }
            }
        }
        fullyResolvedCurrentParam = fullyResolvedCurrentParam2;
        if (display.inFreeformWindowingMode()) {
        }
        launchMode2 = launchMode;
        outParams.mWindowingMode = launchMode2 != display.getWindowingMode() ? 0 : launchMode2;
        if (phase != 1) {
        }
    }

    private int getPreferredLaunchDisplay(TaskRecord task, ActivityOptions options, ActivityRecord source, LaunchParamsController.LaunchParams currentParams) {
        if (!this.mSupervisor.mService.mSupportsMultiDisplay) {
            return 0;
        }
        int displayId = -1;
        int optionLaunchId = options != null ? options.getLaunchDisplayId() : -1;
        if (optionLaunchId != -1) {
            displayId = optionLaunchId;
        }
        if (displayId == -1 && source != null && source.noDisplay) {
            displayId = source.mHandoverLaunchDisplayId;
        }
        ActivityStack stack = (displayId != -1 || task == null) ? null : task.getStack();
        if (stack != null) {
            displayId = stack.mDisplayId;
        }
        if (displayId == -1 && source != null) {
            displayId = source.getDisplayId();
        }
        if (displayId != -1 && this.mSupervisor.mRootActivityContainer.getActivityDisplay(displayId) == null) {
            displayId = currentParams.mPreferredDisplayId;
        }
        int displayId2 = displayId == -1 ? currentParams.mPreferredDisplayId : displayId;
        if (displayId2 == -1 || this.mSupervisor.mRootActivityContainer.getActivityDisplay(displayId2) == null) {
            return 0;
        }
        return displayId2;
    }

    private boolean canApplyFreeformWindowPolicy(ActivityDisplay display, int launchMode) {
        return this.mSupervisor.mService.mSupportsFreeformWindowManagement && (display.inFreeformWindowingMode() || launchMode == 5);
    }

    private boolean canApplyPipWindowPolicy(int launchMode) {
        return this.mSupervisor.mService.mSupportsPictureInPicture && launchMode == 2;
    }

    private void getLayoutBounds(ActivityDisplay display, ActivityRecord root, ActivityInfo.WindowLayout windowLayout, Rect outBounds) {
        int height;
        int width;
        float fractionOfHorizontalOffset;
        float fractionOfVerticalOffset;
        int verticalGravity = windowLayout.gravity & HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE;
        int horizontalGravity = windowLayout.gravity & 7;
        if (!windowLayout.hasSpecifiedSize() && verticalGravity == 0 && horizontalGravity == 0) {
            outBounds.setEmpty();
            return;
        }
        Rect bounds = display.getBounds();
        int defaultWidth = bounds.width();
        int defaultHeight = bounds.height();
        if (!windowLayout.hasSpecifiedSize()) {
            outBounds.setEmpty();
            getTaskBounds(root, display, windowLayout, 5, false, outBounds);
            width = outBounds.width();
            height = outBounds.height();
        } else {
            width = defaultWidth;
            if (windowLayout.width > 0 && windowLayout.width < defaultWidth) {
                width = windowLayout.width;
            } else if (windowLayout.widthFraction > OppoBrightUtils.MIN_LUX_LIMITI && windowLayout.widthFraction < 1.0f) {
                width = (int) (((float) width) * windowLayout.widthFraction);
            }
            height = defaultHeight;
            if (windowLayout.height > 0 && windowLayout.height < defaultHeight) {
                height = windowLayout.height;
            } else if (windowLayout.heightFraction > OppoBrightUtils.MIN_LUX_LIMITI && windowLayout.heightFraction < 1.0f) {
                height = (int) (((float) height) * windowLayout.heightFraction);
            }
        }
        if (horizontalGravity == 3) {
            fractionOfHorizontalOffset = OppoBrightUtils.MIN_LUX_LIMITI;
        } else if (horizontalGravity != 5) {
            fractionOfHorizontalOffset = 0.5f;
        } else {
            fractionOfHorizontalOffset = 1.0f;
        }
        if (verticalGravity == 48) {
            fractionOfVerticalOffset = OppoBrightUtils.MIN_LUX_LIMITI;
        } else if (verticalGravity != 80) {
            fractionOfVerticalOffset = 0.5f;
        } else {
            fractionOfVerticalOffset = 1.0f;
        }
        outBounds.set(0, 0, width, height);
        outBounds.offset((int) (((float) (defaultWidth - width)) * fractionOfHorizontalOffset), (int) (((float) (defaultHeight - height)) * fractionOfVerticalOffset));
    }

    private boolean isTaskForcedMaximized(ActivityRecord root) {
        if (root.appInfo.targetSdkVersion < 4 || (root.appInfo.flags & SUPPORTS_SCREEN_RESIZEABLE_MASK) == 0) {
            return true;
        }
        return !root.isResizeable();
    }

    private int resolveOrientation(ActivityRecord activity) {
        int orientation = activity.info.screenOrientation;
        if (orientation != 0) {
            if (orientation != 1) {
                if (orientation != 11) {
                    if (orientation != 12) {
                        if (orientation != 14) {
                            switch (orientation) {
                                case 5:
                                    break;
                                case 6:
                                case 8:
                                    break;
                                case 7:
                                case 9:
                                    break;
                                default:
                                    return -1;
                            }
                        }
                        return 14;
                    }
                }
            }
            return 1;
        }
        return 0;
    }

    private void cascadeBounds(Rect srcBounds, ActivityDisplay display, Rect outBounds) {
        outBounds.set(srcBounds);
        int defaultOffset = (int) ((75.0f * (((float) display.getConfiguration().densityDpi) / 160.0f)) + 0.5f);
        display.getBounds(this.mTmpBounds);
        outBounds.offset(Math.min(defaultOffset, Math.max(0, this.mTmpBounds.right - srcBounds.right)), Math.min(defaultOffset, Math.max(0, this.mTmpBounds.bottom - srcBounds.bottom)));
    }

    private void getTaskBounds(ActivityRecord root, ActivityDisplay display, ActivityInfo.WindowLayout layout, int resolvedMode, boolean hasInitialBounds, Rect inOutBounds) {
        if (resolvedMode == 1) {
            inOutBounds.setEmpty();
        } else if (resolvedMode == 5) {
            int orientation = resolveOrientation(root, display, inOutBounds);
            if (orientation == 1 || orientation == 0) {
                getDefaultFreeformSize(display, layout, orientation, this.mTmpBounds);
                if (!hasInitialBounds && !sizeMatches(inOutBounds, this.mTmpBounds)) {
                    centerBounds(display, this.mTmpBounds.width(), this.mTmpBounds.height(), inOutBounds);
                    adjustBoundsToFitInDisplay(display, inOutBounds);
                } else if (orientation != orientationFromBounds(inOutBounds)) {
                    centerBounds(display, inOutBounds.height(), inOutBounds.width(), inOutBounds);
                }
                adjustBoundsToAvoidConflictInDisplay(display, inOutBounds);
                return;
            }
            throw new IllegalStateException("Orientation must be one of portrait or landscape, but it's " + ActivityInfo.screenOrientationToString(orientation));
        }
    }

    private int convertOrientationToScreenOrientation(int orientation) {
        if (orientation == 1) {
            return 1;
        }
        if (orientation != 2) {
            return -1;
        }
        return 0;
    }

    private int resolveOrientation(ActivityRecord root, ActivityDisplay display, Rect bounds) {
        int orientation;
        int i;
        int orientation2 = resolveOrientation(root);
        if (orientation2 == 14) {
            if (bounds.isEmpty()) {
                i = convertOrientationToScreenOrientation(display.getConfiguration().orientation);
            } else {
                i = orientationFromBounds(bounds);
            }
            orientation2 = i;
        }
        if (orientation2 != -1) {
            return orientation2;
        }
        if (bounds.isEmpty()) {
            orientation = 1;
        } else {
            orientation = orientationFromBounds(bounds);
        }
        return orientation;
    }

    private void getDefaultFreeformSize(ActivityDisplay display, ActivityInfo.WindowLayout layout, int orientation, Rect bounds) {
        int defaultWidth;
        int defaultHeight;
        int phoneWidth;
        int phoneHeight;
        Rect displayBounds = display.getBounds();
        int portraitHeight = Math.min(displayBounds.width(), displayBounds.height());
        int portraitWidth = (portraitHeight * portraitHeight) / Math.max(displayBounds.width(), displayBounds.height());
        if (orientation == 0) {
            defaultWidth = portraitHeight;
        } else {
            defaultWidth = portraitWidth;
        }
        if (orientation == 0) {
            defaultHeight = portraitWidth;
        } else {
            defaultHeight = portraitHeight;
        }
        float density = ((float) display.getConfiguration().densityDpi) / 160.0f;
        int phonePortraitWidth = (int) ((412.0f * density) + 0.5f);
        int phonePortraitHeight = (int) ((732.0f * density) + 0.5f);
        if (orientation == 0) {
            phoneWidth = phonePortraitHeight;
        } else {
            phoneWidth = phonePortraitWidth;
        }
        if (orientation == 0) {
            phoneHeight = phonePortraitWidth;
        } else {
            phoneHeight = phonePortraitHeight;
        }
        int layoutMinHeight = -1;
        int layoutMinWidth = layout == null ? -1 : layout.minWidth;
        if (layout != null) {
            layoutMinHeight = layout.minHeight;
        }
        bounds.set(0, 0, Math.min(defaultWidth, Math.max(phoneWidth, layoutMinWidth)), Math.min(defaultHeight, Math.max(phoneHeight, layoutMinHeight)));
    }

    private void centerBounds(ActivityDisplay display, int width, int height, Rect inOutBounds) {
        if (inOutBounds.isEmpty()) {
            display.getBounds(inOutBounds);
        }
        int left = inOutBounds.centerX() - (width / 2);
        int top = inOutBounds.centerY() - (height / 2);
        inOutBounds.set(left, top, left + width, top + height);
    }

    private void adjustBoundsToFitInDisplay(ActivityDisplay display, Rect inOutBounds) {
        int left;
        int dx;
        int dy;
        Rect displayBounds = display.getBounds();
        if (displayBounds.width() < inOutBounds.width() || displayBounds.height() < inOutBounds.height()) {
            if (this.mSupervisor.mRootActivityContainer.getConfiguration().getLayoutDirection() == 1) {
                left = displayBounds.width() - inOutBounds.width();
            } else {
                left = 0;
            }
            inOutBounds.offsetTo(left, 0);
            return;
        }
        if (inOutBounds.right > displayBounds.right) {
            dx = displayBounds.right - inOutBounds.right;
        } else if (inOutBounds.left < displayBounds.left) {
            dx = displayBounds.left - inOutBounds.left;
        } else {
            dx = 0;
        }
        if (inOutBounds.top < displayBounds.top) {
            dy = displayBounds.top - inOutBounds.top;
        } else if (inOutBounds.bottom > displayBounds.bottom) {
            dy = displayBounds.bottom - inOutBounds.bottom;
        } else {
            dy = 0;
        }
        inOutBounds.offset(dx, dy);
    }

    private void adjustBoundsToAvoidConflictInDisplay(ActivityDisplay display, Rect inOutBounds) {
        List<Rect> taskBoundsToCheck = new ArrayList<>();
        for (int i = 0; i < display.getChildCount(); i++) {
            ActivityStack stack = display.getChildAt(i);
            if (stack.inFreeformWindowingMode()) {
                for (int j = 0; j < stack.getChildCount(); j++) {
                    taskBoundsToCheck.add(stack.getChildAt(j).getBounds());
                }
            }
        }
        adjustBoundsToAvoidConflict(display.getBounds(), taskBoundsToCheck, inOutBounds);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void adjustBoundsToAvoidConflict(Rect displayBounds, List<Rect> taskBoundsToCheck, Rect inOutBounds) {
        if (displayBounds.contains(inOutBounds) && boundsConflict(taskBoundsToCheck, inOutBounds)) {
            calculateCandidateShiftDirections(displayBounds, inOutBounds);
            int[] iArr = this.mTmpDirections;
            for (int direction : iArr) {
                if (direction != 0) {
                    this.mTmpBounds.set(inOutBounds);
                    while (boundsConflict(taskBoundsToCheck, this.mTmpBounds) && displayBounds.contains(this.mTmpBounds)) {
                        shiftBounds(direction, displayBounds, this.mTmpBounds);
                    }
                    if (!boundsConflict(taskBoundsToCheck, this.mTmpBounds) && displayBounds.contains(this.mTmpBounds)) {
                        inOutBounds.set(this.mTmpBounds);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void calculateCandidateShiftDirections(Rect availableBounds, Rect initialBounds) {
        int i = 0;
        while (true) {
            int[] iArr = this.mTmpDirections;
            if (i >= iArr.length) {
                break;
            }
            iArr[i] = 0;
            i++;
        }
        int oneThirdWidth = ((availableBounds.left * 2) + availableBounds.right) / 3;
        int twoThirdWidth = (availableBounds.left + (availableBounds.right * 2)) / 3;
        int centerX = initialBounds.centerX();
        if (centerX < oneThirdWidth) {
            this.mTmpDirections[0] = 5;
        } else if (centerX > twoThirdWidth) {
            this.mTmpDirections[0] = 3;
        } else {
            int oneThirdHeight = ((availableBounds.top * 2) + availableBounds.bottom) / 3;
            int twoThirdHeight = (availableBounds.top + (availableBounds.bottom * 2)) / 3;
            int centerY = initialBounds.centerY();
            if (centerY < oneThirdHeight || centerY > twoThirdHeight) {
                int[] iArr2 = this.mTmpDirections;
                iArr2[0] = 5;
                iArr2[1] = 3;
                return;
            }
            int[] iArr3 = this.mTmpDirections;
            iArr3[0] = 85;
            iArr3[1] = 51;
        }
    }

    private boolean boundsConflict(List<Rect> taskBoundsToCheck, Rect candidateBounds) {
        Iterator<Rect> it = taskBoundsToCheck.iterator();
        while (true) {
            boolean bottomClose = false;
            if (!it.hasNext()) {
                return false;
            }
            Rect taskBounds = it.next();
            boolean leftClose = Math.abs(taskBounds.left - candidateBounds.left) < 4;
            boolean topClose = Math.abs(taskBounds.top - candidateBounds.top) < 4;
            boolean rightClose = Math.abs(taskBounds.right - candidateBounds.right) < 4;
            if (Math.abs(taskBounds.bottom - candidateBounds.bottom) < 4) {
                bottomClose = true;
            }
            if ((!leftClose || !topClose) && ((!leftClose || !bottomClose) && ((!rightClose || !topClose) && (!rightClose || !bottomClose)))) {
            }
        }
        return true;
    }

    private void shiftBounds(int direction, Rect availableRect, Rect inOutBounds) {
        int horizontalOffset;
        int verticalOffset;
        int i = direction & 7;
        if (i == 3) {
            horizontalOffset = -Math.max(1, availableRect.width() / 16);
        } else if (i != 5) {
            horizontalOffset = 0;
        } else {
            horizontalOffset = Math.max(1, availableRect.width() / 16);
        }
        int i2 = direction & HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE;
        if (i2 == 48) {
            verticalOffset = -Math.max(1, availableRect.height() / 16);
        } else if (i2 != 80) {
            verticalOffset = 0;
        } else {
            verticalOffset = Math.max(1, availableRect.height() / 16);
        }
        inOutBounds.offset(horizontalOffset, verticalOffset);
    }

    private void initLogBuilder(TaskRecord task, ActivityRecord activity) {
    }

    private void appendLog(String log) {
    }

    private void outputLog() {
    }

    private static int orientationFromBounds(Rect bounds) {
        if (bounds.width() > bounds.height()) {
            return 0;
        }
        return 1;
    }

    private static boolean sizeMatches(Rect left, Rect right) {
        return Math.abs(right.width() - left.width()) < 2 && Math.abs(right.height() - left.height()) < 2;
    }
}

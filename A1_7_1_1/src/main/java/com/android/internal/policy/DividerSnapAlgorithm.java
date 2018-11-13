package com.android.internal.policy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.view.DisplayInfo;
import com.android.internal.R;
import java.util.ArrayList;

public class DividerSnapAlgorithm {
    private static final int MIN_DISMISS_VELOCITY_DP_PER_SECOND = 600;
    private static final int MIN_FLING_VELOCITY_DP_PER_SECOND = 400;
    private static final int SNAP_FIXED_RATIO = 1;
    private static final int SNAP_MODE_16_9 = 0;
    private static final int SNAP_ONLY_1_1 = 2;
    private static Context mContext;
    private final SnapTarget mDismissEndTarget;
    private final SnapTarget mDismissStartTarget;
    private final int mDisplayHeight;
    private final int mDisplayWidth;
    private final int mDividerSize;
    private final SnapTarget mFirstSplitTarget;
    private final float mFixedRatio;
    private boolean mHasThreeSnapTargets = true;
    private final Rect mInsets = new Rect();
    private boolean mIsHorizontalDivision;
    private final SnapTarget mLastSplitTarget;
    private final SnapTarget mMiddleTarget;
    private final float mMinDismissVelocityPxPerSecond;
    private final float mMinFlingVelocityPxPerSecond;
    private final int mMinimalSizeResizableTask;
    private final SnapTarget mOrigFirstSplitTarget;
    private final SnapTarget mOrigLastSplitTarget;
    private final ArrayList<SnapTarget> mOrigTargets = new ArrayList();
    private final int mSnapMode;
    private final ArrayList<SnapTarget> mTargets = new ArrayList();

    public static class SnapTarget {
        public static final int FLAG_DISMISS_END = 2;
        public static final int FLAG_DISMISS_START = 1;
        public static final int FLAG_NONE = 0;
        private final float distanceMultiplier;
        public final int flag;
        public final int position;
        public final int taskPosition;

        public SnapTarget(int position, int taskPosition, int flag) {
            this(position, taskPosition, flag, 1.0f);
        }

        public SnapTarget(int position, int taskPosition, int flag, float distanceMultiplier) {
            this.position = position;
            this.taskPosition = taskPosition;
            this.flag = flag;
            this.distanceMultiplier = distanceMultiplier;
        }
    }

    public static DividerSnapAlgorithm create(Context ctx, Rect insets) {
        boolean z = true;
        mContext = ctx;
        DisplayInfo displayInfo = new DisplayInfo();
        ((DisplayManager) ctx.getSystemService(DisplayManager.class)).getDisplay(0).getDisplayInfo(displayInfo);
        int dividerWindowWidth = ctx.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_thickness);
        int dividerInsets = ctx.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_insets);
        Resources resources = ctx.getResources();
        int i = displayInfo.logicalWidth;
        int i2 = displayInfo.logicalHeight;
        int i3 = dividerWindowWidth - (dividerInsets * 2);
        if (ctx.getApplicationContext().getResources().getConfiguration().orientation != 1) {
            z = false;
        }
        return new DividerSnapAlgorithm(resources, i, i2, i3, z, insets);
    }

    public DividerSnapAlgorithm(Resources res, int displayWidth, int displayHeight, int dividerSize, boolean isHorizontalDivision, Rect insets) {
        this.mMinFlingVelocityPxPerSecond = res.getDisplayMetrics().density * 400.0f;
        this.mMinDismissVelocityPxPerSecond = res.getDisplayMetrics().density * 600.0f;
        this.mDividerSize = dividerSize;
        this.mDisplayWidth = displayWidth;
        this.mDisplayHeight = displayHeight;
        this.mIsHorizontalDivision = isHorizontalDivision;
        this.mInsets.set(insets);
        this.mSnapMode = res.getInteger(R.integer.config_dockedStackDividerSnapMode);
        this.mFixedRatio = res.getFraction(R.fraction.docked_stack_divider_fixed_ratio, 1, 1);
        this.mMinimalSizeResizableTask = res.getDimensionPixelSize(R.dimen.default_minimal_size_resizable_task);
        calculateTargets(isHorizontalDivision);
        this.mFirstSplitTarget = (SnapTarget) this.mTargets.get(1);
        this.mLastSplitTarget = (SnapTarget) this.mTargets.get(this.mTargets.size() - 2);
        this.mDismissStartTarget = (SnapTarget) this.mTargets.get(0);
        this.mDismissEndTarget = (SnapTarget) this.mTargets.get(this.mTargets.size() - 1);
        this.mMiddleTarget = (SnapTarget) this.mTargets.get(this.mTargets.size() / 2);
        if (this.mHasThreeSnapTargets) {
            this.mOrigFirstSplitTarget = this.mFirstSplitTarget;
            this.mOrigLastSplitTarget = this.mLastSplitTarget;
            int size = this.mTargets.size();
            for (int i = 0; i < size; i++) {
                this.mOrigTargets.add((SnapTarget) this.mTargets.get(i));
            }
            return;
        }
        this.mOrigFirstSplitTarget = this.mMiddleTarget;
        this.mOrigLastSplitTarget = this.mMiddleTarget;
        this.mOrigTargets.add(this.mDismissStartTarget);
        this.mOrigTargets.add(this.mMiddleTarget);
        this.mOrigTargets.add(this.mDismissEndTarget);
    }

    public boolean isSplitScreenFeasible() {
        int size;
        int statusBarSize = this.mInsets.top;
        int navBarSize = this.mIsHorizontalDivision ? this.mInsets.bottom : this.mInsets.right;
        if (this.mIsHorizontalDivision) {
            size = this.mDisplayHeight;
        } else {
            size = this.mDisplayWidth;
        }
        if ((((size - navBarSize) - statusBarSize) - this.mDividerSize) / 2 >= this.mMinimalSizeResizableTask) {
            return true;
        }
        return false;
    }

    public SnapTarget calculateSnapTarget(int position, float velocity) {
        return calculateSnapTarget(position, velocity, true);
    }

    public SnapTarget calculateSnapTarget(int position, float velocity, boolean hardDismiss) {
        if (isFullFunctionMode()) {
            if (position < this.mOrigFirstSplitTarget.position && velocity < (-this.mMinDismissVelocityPxPerSecond)) {
                return this.mDismissStartTarget;
            }
            if (position > this.mOrigLastSplitTarget.position && velocity > this.mMinDismissVelocityPxPerSecond) {
                return this.mDismissEndTarget;
            }
            if (Math.abs(velocity) < this.mMinFlingVelocityPxPerSecond) {
                return snap(position, hardDismiss);
            }
            if (velocity < 0.0f) {
                return this.mOrigFirstSplitTarget;
            }
            return this.mOrigLastSplitTarget;
        } else if (position < this.mFirstSplitTarget.position && velocity < (-this.mMinDismissVelocityPxPerSecond)) {
            return this.mDismissStartTarget;
        } else {
            if (position > this.mLastSplitTarget.position && velocity > this.mMinDismissVelocityPxPerSecond) {
                return this.mDismissEndTarget;
            }
            if (Math.abs(velocity) < this.mMinFlingVelocityPxPerSecond) {
                return snap(position, hardDismiss);
            }
            if (velocity < 0.0f) {
                return this.mFirstSplitTarget;
            }
            return this.mLastSplitTarget;
        }
    }

    public SnapTarget calculateNonDismissingSnapTarget(int position) {
        SnapTarget target = snap(position, false);
        if (isFullFunctionMode()) {
            if (target == this.mDismissStartTarget) {
                return this.mOrigFirstSplitTarget;
            }
            if (target == this.mDismissEndTarget) {
                return this.mOrigLastSplitTarget;
            }
            return target;
        } else if (target == this.mDismissStartTarget) {
            return this.mFirstSplitTarget;
        } else {
            if (target == this.mDismissEndTarget) {
                return this.mLastSplitTarget;
            }
            return target;
        }
    }

    public float calculateDismissingFraction(int position) {
        if (isFullFunctionMode()) {
            if (position < this.mOrigFirstSplitTarget.position) {
                return 1.0f - (((float) (position - getStartInset())) / ((float) (this.mOrigFirstSplitTarget.position - getStartInset())));
            }
            if (position > this.mOrigLastSplitTarget.position) {
                return ((float) (position - this.mOrigLastSplitTarget.position)) / ((float) ((this.mDismissEndTarget.position - this.mOrigLastSplitTarget.position) - this.mDividerSize));
            }
            return 0.0f;
        } else if (position < this.mFirstSplitTarget.position) {
            return 1.0f - (((float) (position - getStartInset())) / ((float) (this.mFirstSplitTarget.position - getStartInset())));
        } else {
            if (position > this.mLastSplitTarget.position) {
                return ((float) (position - this.mLastSplitTarget.position)) / ((float) ((this.mDismissEndTarget.position - this.mLastSplitTarget.position) - this.mDividerSize));
            }
            return 0.0f;
        }
    }

    public SnapTarget getClosestDismissTarget(int position) {
        if (isFullFunctionMode()) {
            if (position < this.mOrigFirstSplitTarget.position) {
                return this.mDismissStartTarget;
            }
            if (position > this.mOrigLastSplitTarget.position) {
                return this.mDismissEndTarget;
            }
            if (position - this.mDismissStartTarget.position < this.mDismissEndTarget.position - position) {
                return this.mDismissStartTarget;
            }
            return this.mDismissEndTarget;
        } else if (position < this.mFirstSplitTarget.position) {
            return this.mDismissStartTarget;
        } else {
            if (position > this.mLastSplitTarget.position) {
                return this.mDismissEndTarget;
            }
            if (position - this.mDismissStartTarget.position < this.mDismissEndTarget.position - position) {
                return this.mDismissStartTarget;
            }
            return this.mDismissEndTarget;
        }
    }

    public SnapTarget getFirstSplitTarget() {
        if (isFullFunctionMode()) {
            return this.mOrigFirstSplitTarget;
        }
        return this.mFirstSplitTarget;
    }

    public SnapTarget getLastSplitTarget() {
        if (isFullFunctionMode()) {
            return this.mOrigLastSplitTarget;
        }
        return this.mLastSplitTarget;
    }

    public SnapTarget getDismissStartTarget() {
        return this.mDismissStartTarget;
    }

    public SnapTarget getDismissEndTarget() {
        return this.mDismissEndTarget;
    }

    private int getStartInset() {
        if (this.mIsHorizontalDivision) {
            return this.mInsets.top;
        }
        return this.mInsets.left;
    }

    private int getEndInset() {
        if (this.mIsHorizontalDivision) {
            return this.mInsets.bottom;
        }
        return this.mInsets.right;
    }

    private SnapTarget snap(int position, boolean hardDismiss) {
        int minIndex = -1;
        float minDistance = Float.MAX_VALUE;
        int size;
        int i;
        SnapTarget target;
        float distance;
        if (isFullFunctionMode()) {
            size = this.mOrigTargets.size();
            for (i = 0; i < size; i++) {
                target = (SnapTarget) this.mOrigTargets.get(i);
                distance = (float) Math.abs(position - target.position);
                if (hardDismiss) {
                    distance /= target.distanceMultiplier;
                }
                if (distance < minDistance) {
                    minIndex = i;
                    minDistance = distance;
                }
            }
            return (SnapTarget) this.mOrigTargets.get(minIndex);
        }
        size = this.mTargets.size();
        for (i = 0; i < size; i++) {
            target = (SnapTarget) this.mTargets.get(i);
            distance = (float) Math.abs(position - target.position);
            if (hardDismiss) {
                distance /= target.distanceMultiplier;
            }
            if (distance < minDistance) {
                minIndex = i;
                minDistance = distance;
            }
        }
        return (SnapTarget) this.mTargets.get(minIndex);
    }

    private void calculateTargets(boolean isHorizontalDivision) {
        int dividerMax;
        this.mTargets.clear();
        this.mOrigTargets.clear();
        if (isHorizontalDivision) {
            dividerMax = this.mDisplayHeight;
        } else {
            dividerMax = this.mDisplayWidth;
        }
        this.mTargets.add(new SnapTarget(-this.mDividerSize, -this.mDividerSize, 1, 0.35f));
        switch (this.mSnapMode) {
            case 0:
                addRatio16_9Targets(isHorizontalDivision, dividerMax);
                break;
            case 1:
                addFixedDivisionTargets(isHorizontalDivision, dividerMax);
                break;
            case 2:
                addMiddleTarget(isHorizontalDivision);
                break;
        }
        this.mTargets.add(new SnapTarget(dividerMax - (isHorizontalDivision ? this.mInsets.bottom : this.mInsets.right), dividerMax, 2, 0.35f));
    }

    private void addNonDismissingTargets(boolean isHorizontalDivision, int topPosition, int bottomPosition, int dividerMax) {
        maybeAddTarget(topPosition, topPosition - this.mInsets.top);
        addMiddleTarget(isHorizontalDivision);
        maybeAddTarget(bottomPosition, (dividerMax - this.mInsets.bottom) - (this.mDividerSize + bottomPosition));
    }

    private void addFixedDivisionTargets(boolean isHorizontalDivision, int dividerMax) {
        int end;
        int start = isHorizontalDivision ? this.mInsets.top : this.mInsets.left;
        if (isHorizontalDivision) {
            end = this.mDisplayHeight - this.mInsets.bottom;
        } else {
            end = this.mDisplayWidth - this.mInsets.right;
        }
        int size = ((int) (this.mFixedRatio * ((float) (end - start)))) - (this.mDividerSize / 2);
        addNonDismissingTargets(isHorizontalDivision, start + size, (end - size) - this.mDividerSize, dividerMax);
    }

    private void addRatio16_9Targets(boolean isHorizontalDivision, int dividerMax) {
        int end;
        int endOther;
        int start = isHorizontalDivision ? this.mInsets.top : this.mInsets.left;
        if (isHorizontalDivision) {
            end = this.mDisplayHeight - this.mInsets.bottom;
        } else {
            end = this.mDisplayWidth - this.mInsets.right;
        }
        int startOther = isHorizontalDivision ? this.mInsets.left : this.mInsets.top;
        if (isHorizontalDivision) {
            endOther = this.mDisplayWidth - this.mInsets.right;
        } else {
            endOther = this.mDisplayHeight - this.mInsets.bottom;
        }
        int sizeInt = (int) Math.floor((double) (0.5625f * ((float) (endOther - startOther))));
        addNonDismissingTargets(isHorizontalDivision, start + sizeInt, (end - sizeInt) - this.mDividerSize, dividerMax);
    }

    private void maybeAddTarget(int position, int smallerSize) {
        if (smallerSize >= this.mMinimalSizeResizableTask) {
            this.mTargets.add(new SnapTarget(position, position, 0));
            return;
        }
        this.mTargets.add(new SnapTarget(position, position, 0));
        this.mHasThreeSnapTargets = false;
    }

    private void addMiddleTarget(boolean isHorizontalDivision) {
        int position = DockedDividerUtils.calculateMiddlePosition(isHorizontalDivision, this.mInsets, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
        this.mTargets.add(new SnapTarget(position, position, 0));
    }

    public SnapTarget getMiddleTarget() {
        return this.mMiddleTarget;
    }

    public SnapTarget getNextTarget(SnapTarget snapTarget) {
        int index;
        if (isFullFunctionMode()) {
            index = this.mOrigTargets.indexOf(snapTarget);
            if (index == -1 || index >= this.mOrigTargets.size() - 1) {
                return snapTarget;
            }
            return (SnapTarget) this.mOrigTargets.get(index + 1);
        }
        index = this.mTargets.indexOf(snapTarget);
        if (index == -1 || index >= this.mTargets.size() - 1) {
            return snapTarget;
        }
        return (SnapTarget) this.mTargets.get(index + 1);
    }

    public SnapTarget getPreviousTarget(SnapTarget snapTarget) {
        int index;
        if (isFullFunctionMode()) {
            index = this.mOrigTargets.indexOf(snapTarget);
            if (index == -1 || index <= 0) {
                return snapTarget;
            }
            return (SnapTarget) this.mOrigTargets.get(index - 1);
        }
        index = this.mTargets.indexOf(snapTarget);
        if (index == -1 || index <= 0) {
            return snapTarget;
        }
        return (SnapTarget) this.mTargets.get(index - 1);
    }

    public boolean isFirstSplitTargetAvailable() {
        boolean z = true;
        if (isFullFunctionMode()) {
            if (this.mOrigFirstSplitTarget == this.mMiddleTarget) {
                z = false;
            }
            return z;
        }
        if (this.mFirstSplitTarget == this.mMiddleTarget) {
            z = false;
        }
        return z;
    }

    public boolean isLastSplitTargetAvailable() {
        boolean z = true;
        if (isFullFunctionMode()) {
            if (this.mOrigLastSplitTarget == this.mMiddleTarget) {
                z = false;
            }
            return z;
        }
        if (this.mLastSplitTarget == this.mMiddleTarget) {
            z = false;
        }
        return z;
    }

    public SnapTarget cycleNonDismissTarget(SnapTarget snapTarget, int increment) {
        int index;
        SnapTarget newTarget;
        if (isFullFunctionMode()) {
            index = this.mOrigTargets.indexOf(snapTarget);
            if (index == -1) {
                return snapTarget;
            }
            newTarget = (SnapTarget) this.mOrigTargets.get(((this.mOrigTargets.size() + index) + increment) % this.mOrigTargets.size());
            if (newTarget == this.mDismissStartTarget) {
                return this.mOrigLastSplitTarget;
            }
            if (newTarget == this.mDismissEndTarget) {
                return this.mOrigFirstSplitTarget;
            }
            return newTarget;
        }
        index = this.mTargets.indexOf(snapTarget);
        if (index == -1) {
            return snapTarget;
        }
        newTarget = (SnapTarget) this.mTargets.get(((this.mTargets.size() + index) + increment) % this.mTargets.size());
        if (newTarget == this.mDismissStartTarget) {
            return this.mLastSplitTarget;
        }
        if (newTarget == this.mDismissEndTarget) {
            return this.mFirstSplitTarget;
        }
        return newTarget;
    }

    private static boolean isFullFunctionMode() {
        return mContext != null ? mContext.getPackageManager().isFullFunctionMode() : false;
    }
}

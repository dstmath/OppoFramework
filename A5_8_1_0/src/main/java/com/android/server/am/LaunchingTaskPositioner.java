package com.android.server.am;

import android.content.pm.ActivityInfo.WindowLayout;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import com.android.server.display.OppoBrightUtils;
import java.util.ArrayList;

class LaunchingTaskPositioner {
    private static final boolean ALLOW_RESTART = true;
    private static final int BOUNDS_CONFLICT_MIN_DISTANCE = 4;
    private static int DEFAULT_DENSITY = 3;
    private static int DEFAULT_FREEFORM_HEIGHT = 314;
    private static int DEFAULT_FREEFORM_WIDTH = 296;
    private static int DEFAULT_FREEFORM_X_0 = 14;
    private static int DEFAULT_FREEFORM_X_90 = 82;
    private static int DEFAULT_FREEFORM_Y_0 = 108;
    private static int DEFAULT_FREEFORM_Y_90 = 26;
    private static final int MARGIN_SIZE_DENOMINATOR = 4;
    private static final int MINIMAL_STEP = 1;
    private static int SCREEN_HEIGHT = 2280;
    private static final int SHIFT_POLICY_DIAGONAL_DOWN = 1;
    private static final int SHIFT_POLICY_HORIZONTAL_LEFT = 3;
    private static final int SHIFT_POLICY_HORIZONTAL_RIGHT = 2;
    private static final int STEP_DENOMINATOR = 16;
    private static final String TAG = "ActivityManager";
    private static final int WINDOW_SIZE_DENOMINATOR = 2;
    private static int mDensity = 3;
    private static int mDiviceLogicalDisplayHeight = 0;
    private final Rect mAvailableRect = new Rect();
    private int mDefaultFreeformHeight;
    private int mDefaultFreeformStartX;
    private int mDefaultFreeformStartY;
    private int mDefaultFreeformStepHorizontal;
    private int mDefaultFreeformStepVertical;
    private int mDefaultFreeformWidth;
    private boolean mDefaultStartBoundsConfigurationSet = false;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private int mRotation;
    private final Rect mTmpOriginal = new Rect();
    private final Rect mTmpProposal = new Rect();

    LaunchingTaskPositioner() {
    }

    void setDisplay(Display display) {
        Point size = new Point();
        display.getSize(size);
        this.mDisplayWidth = size.x;
        this.mDisplayHeight = size.y;
        this.mRotation = display.getRotation();
        mDensity = DisplayMetrics.DENSITY_DEVICE > 0 ? DisplayMetrics.DENSITY_DEVICE / 160 : DEFAULT_DENSITY;
        int height = getDeviceLogicalDisplayHeight();
        if (height <= 0) {
            height = SCREEN_HEIGHT;
        }
        mDiviceLogicalDisplayHeight = height;
    }

    void configure(Rect stackBounds) {
        if (stackBounds == null) {
            this.mAvailableRect.set(0, 0, this.mDisplayWidth, this.mDisplayHeight);
        } else {
            this.mAvailableRect.set(stackBounds);
        }
        int width = this.mAvailableRect.width();
        int height = this.mAvailableRect.height();
        this.mDefaultFreeformWidth = DEFAULT_FREEFORM_WIDTH * mDensity;
        this.mDefaultFreeformHeight = DEFAULT_FREEFORM_HEIGHT * mDensity;
        if (this.mRotation == 0 || this.mRotation == 2) {
            this.mDefaultFreeformStartX = DEFAULT_FREEFORM_X_0 * mDensity;
            this.mDefaultFreeformStartY = DEFAULT_FREEFORM_Y_0 * mDensity;
        } else if (this.mRotation == 1) {
            this.mDefaultFreeformStartX = DEFAULT_FREEFORM_X_90 * mDensity;
            this.mDefaultFreeformStartY = DEFAULT_FREEFORM_Y_90 * mDensity;
        } else if (this.mRotation == 3) {
            this.mDefaultFreeformStartX = (mDiviceLogicalDisplayHeight - this.mDefaultFreeformWidth) - (DEFAULT_FREEFORM_X_90 * mDensity);
            this.mDefaultFreeformStartY = DEFAULT_FREEFORM_Y_90 * mDensity;
        }
        this.mDefaultFreeformStepHorizontal = Math.max(width / 16, 1);
        this.mDefaultFreeformStepVertical = Math.max(height / 16, 1);
        this.mDefaultStartBoundsConfigurationSet = true;
    }

    void updateDefaultBounds(TaskRecord task, ArrayList<TaskRecord> tasks, WindowLayout windowLayout) {
        if (!this.mDefaultStartBoundsConfigurationSet) {
            return;
        }
        if (windowLayout == null) {
            positionCenter(task, tasks, this.mDefaultFreeformWidth, this.mDefaultFreeformHeight);
            return;
        }
        int width = getFinalWidth(windowLayout);
        int height = getFinalHeight(windowLayout);
        int verticalGravity = windowLayout.gravity & 112;
        int horizontalGravity = windowLayout.gravity & 7;
        if (verticalGravity == 48) {
            if (horizontalGravity == 5) {
                positionTopRight(task, tasks, width, height);
            } else {
                positionTopLeft(task, tasks, width, height);
            }
        } else if (verticalGravity != 80) {
            Slog.w(TAG, "Received unsupported gravity: " + windowLayout.gravity + ", positioning in the center instead.");
            positionCenter(task, tasks, width, height);
        } else if (horizontalGravity == 5) {
            positionBottomRight(task, tasks, width, height);
        } else {
            positionBottomLeft(task, tasks, width, height);
        }
    }

    private int getFinalWidth(WindowLayout windowLayout) {
        int width = this.mDefaultFreeformWidth;
        if (windowLayout.width > 0) {
            width = windowLayout.width;
        }
        if (windowLayout.widthFraction > OppoBrightUtils.MIN_LUX_LIMITI) {
            return (int) (((float) this.mAvailableRect.width()) * windowLayout.widthFraction);
        }
        return width;
    }

    private int getFinalHeight(WindowLayout windowLayout) {
        int height = this.mDefaultFreeformHeight;
        if (windowLayout.height > 0) {
            height = windowLayout.height;
        }
        if (windowLayout.heightFraction > OppoBrightUtils.MIN_LUX_LIMITI) {
            return (int) (((float) this.mAvailableRect.height()) * windowLayout.heightFraction);
        }
        return height;
    }

    private void positionBottomLeft(TaskRecord task, ArrayList<TaskRecord> tasks, int width, int height) {
        this.mTmpProposal.set(this.mAvailableRect.left, this.mAvailableRect.bottom - height, this.mAvailableRect.left + width, this.mAvailableRect.bottom);
        position(task, tasks, this.mTmpProposal, false, 2);
    }

    private void positionBottomRight(TaskRecord task, ArrayList<TaskRecord> tasks, int width, int height) {
        this.mTmpProposal.set(this.mAvailableRect.right - width, this.mAvailableRect.bottom - height, this.mAvailableRect.right, this.mAvailableRect.bottom);
        position(task, tasks, this.mTmpProposal, false, 3);
    }

    private void positionTopLeft(TaskRecord task, ArrayList<TaskRecord> tasks, int width, int height) {
        this.mTmpProposal.set(this.mAvailableRect.left, this.mAvailableRect.top, this.mAvailableRect.left + width, this.mAvailableRect.top + height);
        position(task, tasks, this.mTmpProposal, false, 2);
    }

    private void positionTopRight(TaskRecord task, ArrayList<TaskRecord> tasks, int width, int height) {
        this.mTmpProposal.set(this.mAvailableRect.right - width, this.mAvailableRect.top, this.mAvailableRect.right, this.mAvailableRect.top + height);
        position(task, tasks, this.mTmpProposal, false, 3);
    }

    private void positionCenter(TaskRecord task, ArrayList<TaskRecord> tasks, int width, int height) {
        this.mTmpProposal.set(this.mDefaultFreeformStartX, this.mDefaultFreeformStartY, this.mDefaultFreeformStartX + width, this.mDefaultFreeformStartY + height);
        position(task, tasks, this.mTmpProposal, true, 1);
    }

    private void position(TaskRecord task, ArrayList<TaskRecord> arrayList, Rect proposal, boolean allowRestart, int shiftPolicy) {
        this.mTmpOriginal.set(proposal);
        task.updateOverrideConfiguration(proposal);
    }

    private boolean shiftedToFar(Rect start, int shiftPolicy) {
        boolean z = true;
        switch (shiftPolicy) {
            case 2:
                if (start.right <= this.mAvailableRect.right) {
                    z = false;
                }
                return z;
            case 3:
                if (start.left >= this.mAvailableRect.left) {
                    z = false;
                }
                return z;
            default:
                if (start.right <= this.mAvailableRect.right && start.bottom <= this.mAvailableRect.bottom) {
                    z = false;
                }
                return z;
        }
    }

    private void shiftStartingPoint(Rect posposal, int shiftPolicy) {
        switch (shiftPolicy) {
            case 2:
                posposal.offset(this.mDefaultFreeformStepHorizontal, 0);
                return;
            case 3:
                posposal.offset(-this.mDefaultFreeformStepHorizontal, 0);
                return;
            default:
                posposal.offset(this.mDefaultFreeformStepHorizontal, this.mDefaultFreeformStepVertical);
                return;
        }
    }

    private static boolean boundsConflict(Rect proposal, ArrayList<TaskRecord> tasks) {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            TaskRecord task = (TaskRecord) tasks.get(i);
            if (!(task.mActivities.isEmpty() || task.mBounds == null)) {
                Rect bounds = task.mBounds;
                if (closeLeftTopCorner(proposal, bounds) || closeRightTopCorner(proposal, bounds) || closeLeftBottomCorner(proposal, bounds) || closeRightBottomCorner(proposal, bounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final boolean closeLeftTopCorner(Rect first, Rect second) {
        if (Math.abs(first.left - second.left) >= 4 || Math.abs(first.top - second.top) >= 4) {
            return false;
        }
        return true;
    }

    private static final boolean closeRightTopCorner(Rect first, Rect second) {
        if (Math.abs(first.right - second.right) >= 4 || Math.abs(first.top - second.top) >= 4) {
            return false;
        }
        return true;
    }

    private static final boolean closeLeftBottomCorner(Rect first, Rect second) {
        if (Math.abs(first.left - second.left) >= 4 || Math.abs(first.bottom - second.bottom) >= 4) {
            return false;
        }
        return true;
    }

    private static final boolean closeRightBottomCorner(Rect first, Rect second) {
        if (Math.abs(first.right - second.right) >= 4 || Math.abs(first.bottom - second.bottom) >= 4) {
            return false;
        }
        return true;
    }

    void reset() {
        this.mDefaultStartBoundsConfigurationSet = false;
    }

    private static int getDeviceLogicalDisplayHeight() {
        if (mDiviceLogicalDisplayHeight == 0) {
            mDiviceLogicalDisplayHeight = Integer.parseInt(SystemProperties.get("persist.sys.oppo.displaymetrics", "0,0").split(",")[1]);
        }
        return mDiviceLogicalDisplayHeight;
    }
}

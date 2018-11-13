package com.android.server.am;

import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import android.widget.Toast;

public class OppoDockedManagerService {
    private static final String FORCED_RESIZED = "stackdivider.ForcedResizableInfoActivity";
    public static final int FULL_STATE = 1;
    public static final int INVALID_STATE = -1;
    public static final int REVERT_STATE = 2;
    private static final String TAG = "OppoDockedManagerService";
    private static final int TEMP_MESSAGE = 1;
    private static final Object mLock = new Object();
    private static OppoDockedManagerService sInstance = null;
    private ActivityManagerService mAms = null;
    private int mFullTaskId;
    private int mFullscreenState = -1;
    private TempHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasFullscreen;
    private Rect mRevertRect = new Rect();

    private class TempHandler extends Handler {
        public TempHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Toast.makeText(OppoDockedManagerService.this.mAms.mContext, 201590184, 0).show();
        }
    }

    public static OppoDockedManagerService getInstance() {
        OppoDockedManagerService oppoDockedManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OppoDockedManagerService();
            }
            oppoDockedManagerService = sInstance;
        }
        return oppoDockedManagerService;
    }

    private OppoDockedManagerService() {
    }

    protected void init(ActivityManagerService ams) {
        this.mAms = ams;
        this.mHandlerThread = new HandlerThread("tempfulllscreen");
        this.mHandlerThread.start();
        this.mHandler = new TempHandler(this.mHandlerThread.getLooper());
    }

    protected boolean handleActivityToFullscreen(ActivityRecord prev, ActivityRecord handleActivity) {
        ActivityStack stack = this.mAms.mStackSupervisor.getStack(3);
        if (!(stack == null || handleActivity == null)) {
            int stackId = handleActivity.getStackId();
            ActivityStack tmp = handleActivity.getStack();
            if (this.mHasFullscreen) {
                if (stackId == 0) {
                    reset();
                    this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
                    if (ActivityManagerDebugConfig.DEBUG_MULTI_WINDOW) {
                        Slog.v(TAG, "handleActivityToFullscreen home stack");
                    }
                    return true;
                } else if (!(handleActivity.getTask() == null || handleActivity.getTask().taskId == this.mFullTaskId || stackId == 5)) {
                    reset();
                    this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
                    if (ActivityManagerDebugConfig.DEBUG_MULTI_WINDOW) {
                        Slog.v(TAG, "handleActivityToFullscreen full stack: " + handleActivity);
                    }
                    return true;
                }
            }
            if (!(handleActivity.realActivity == null || !needFullscreenName(handleActivity.realActivity.getClassName()) || stackId == -1 || tmp == null || (tmp.mFullscreen ^ 1) == 0)) {
                if (this.mRevertRect != null) {
                    this.mRevertRect.setEmpty();
                    this.mRevertRect = stack.mBounds;
                }
                this.mFullscreenState = 1;
                this.mHasFullscreen = true;
                setInFullscreeSplit(true);
                handleActivity.notifyWindowFreezing(true, stackId);
                if (handleActivity.getTask() != null) {
                    this.mFullTaskId = handleActivity.getTask().taskId;
                }
                if (stackId == 1) {
                    this.mAms.mStackSupervisor.resizeStackLocked(1, null, null, null, false, true, false);
                } else if (stackId == 3) {
                    this.mAms.mStackSupervisor.resizeDockedStackLocked(null, null, null, null, null, false, false);
                }
                sendTempHandler();
                if (ActivityManagerDebugConfig.DEBUG_MULTI_WINDOW) {
                    Slog.v(TAG, "handleActivityToFullscreen from docked to temp full: " + handleActivity);
                }
                return true;
            }
        }
        return false;
    }

    public boolean needFullscreenActivity(ActivityRecord handleActivity) {
        if (handleActivity == null || handleActivity.realActivity == null || handleActivity.realActivity.getClassName() == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_MULTI_WINDOW) {
            Slog.v(TAG, "needFullscreenActivity: " + handleActivity.realActivity.getClassName());
        }
        return OppoSplitWindowAppReader.getInstance().needFullscreenName(handleActivity.realActivity.getClassName());
    }

    public boolean needFullscreenName(String shortComponentName) {
        if (shortComponentName == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_MULTI_WINDOW) {
            Slog.v(TAG, "needFullscreenName: " + shortComponentName);
        }
        return OppoSplitWindowAppReader.getInstance().needFullscreenName(shortComponentName);
    }

    public void revertStack(ActivityRecord prev, ActivityRecord resuming) {
        ActivityStack stack = this.mAms.mStackSupervisor.getStack(3, false, false);
        if (prev != null && stack != null && needFullscreenActivity(prev)) {
            int stackId = prev.getStackId();
            setInFullscreeSplit(false);
            prev.notifyWindowFreezing(true, stackId);
            this.mFullscreenState = 2;
            this.mAms.mStackSupervisor.resizeDockedStackLocked(this.mRevertRect, null, null, null, null, true, true);
            this.mHasFullscreen = false;
            this.mFullscreenState = -1;
        }
    }

    protected void setInFullscreeSplit(boolean fullscreen) {
        if (this.mAms.mWindowManager != null) {
            this.mAms.mWindowManager.setInFullscreeSplit(fullscreen);
        }
    }

    public boolean fullscreenForRecent() {
        if (!this.mHasFullscreen) {
            return false;
        }
        reset();
        ActivityStack focuseStack = this.mAms.getFocusedStack();
        if (ActivityManagerDebugConfig.DEBUG_MULTI_WINDOW) {
            Slog.v(TAG, "fullscreenForRecent: " + focuseStack);
        }
        if (focuseStack == null || focuseStack.getStackId() != 3) {
            this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
        } else {
            this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(3, true);
        }
        return true;
    }

    public void handleActivitySwitch(ActivityRecord prev, ActivityRecord next) {
        if (next != null && this.mHasFullscreen && this.mAms.mStackSupervisor.getStack(3, false, false) != null) {
            ActivityStack stack = next.getStack();
            if (stack != null && stack.isHomeStack()) {
                reset();
                this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(3, false);
            }
        }
    }

    public void reset() {
        this.mHasFullscreen = false;
        this.mFullscreenState = -1;
        this.mFullTaskId = -1;
        if (this.mRevertRect != null) {
            this.mRevertRect.setEmpty();
        }
        setInFullscreeSplit(false);
    }

    public boolean getHasFullscreen() {
        return this.mHasFullscreen;
    }

    public int getFullscreenState() {
        return this.mFullscreenState;
    }

    void sendTempHandler() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }
    }
}

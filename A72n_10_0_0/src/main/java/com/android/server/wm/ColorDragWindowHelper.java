package com.android.server.wm;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.MagnificationSpec;
import com.android.server.UiThread;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorDragWindowHelper {
    private static String BREENNO_NAME = "BreenoSmartPanelBg";
    private static String BREENNO_PANEL_NAME = "BreenoSmartPanel";
    private static int BREENO_DRAG_MODE = 2;
    public static final boolean DEBUG = true;
    private static String DRAG_DURATION = "duration";
    private static String DRAG_MODE = "mode";
    private static String DRAG_PKG = "fromPkg";
    private static String DRAG_SCALE = "scale";
    private static int DRAG_STATE_ANIMATING = 3;
    private static int DRAG_STATE_HOLD = 4;
    private static int DRAG_STATE_NORMAL = 1;
    private static int DRAG_STATE_START = 2;
    private static String DRAG_TO_X = "toX";
    private static String DRAG_TO_Y = "toY";
    private static String OPPO_RC = "OPPORC";
    private static final int PA_ANIMATION_STATE_IDLE = 0;
    private static final int PA_ANIMATION_STATE_PLAYING = 1;
    public static final String PERSONAL_ASSISTANT_ANIMATION_STATE = "personal_assistant_animation_state";
    public static final String TAG = "OppoDragWindowHelper";
    private static ColorDragWindowHelper mColorDragWindowHelper;
    public static int mDragState;
    private static final Object mLock = new Object();
    private static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private Runnable dragWindowRun = new Runnable() {
        /* class com.android.server.wm.ColorDragWindowHelper.AnonymousClass1 */

        public void run() {
            ColorDragWindowHelper.this.requestTraversal();
        }
    };
    private boolean mBreeno;
    public ColorDragWindowAnimation mColorDragWindowAnimation;
    private Context mContext;
    public DisplayContent mDefaultDisplayContent;
    private int mDragMode;
    public final MagnificationSpec mMagnificationSpec = MagnificationSpec.obtain();
    private Bundle mOptions;
    private String mPkg;
    private int mResId;
    private boolean mTraversalScheduled;
    private WindowManagerService mWms;

    public static ColorDragWindowHelper getInstance() {
        return mColorDragWindowHelper;
    }

    private ColorDragWindowHelper(Context context, WindowManagerService wms) {
        this.mContext = context;
        this.mWms = wms;
    }

    public static void init(Context context, WindowManagerService wms) {
        if (mColorDragWindowHelper == null) {
            mColorDragWindowHelper = new ColorDragWindowHelper(context, wms);
        }
        mDragState = DRAG_STATE_NORMAL;
    }

    /* access modifiers changed from: protected */
    public void setDefaultDisplay() {
        if (this.mDefaultDisplayContent == null) {
            this.mDefaultDisplayContent = this.mWms.getDefaultDisplayContentLocked();
        }
    }

    public void startColorDragWindow(String pkg, int resId, int mode, Bundle options) {
        this.mDragMode = mode;
        this.mResId = resId;
        this.mPkg = pkg;
        this.mOptions = options;
        if (sDebugfDetail) {
            Slog.v(TAG, "startColorDragWindow resId: " + resId + "=mDragState=" + mDragState + "=mDragMode=" + this.mDragMode);
        }
        int i = mDragState;
        if (i == DRAG_STATE_NORMAL || i == DRAG_STATE_HOLD) {
            requestTraversalToDragWindow();
        }
    }

    public void requestTraversalToDragWindow() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            UiThread.getHandler().postAtFrontOfQueue(this.dragWindowRun);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestTraversal() {
        synchronized (mLock) {
            mDragState = DRAG_STATE_START;
            this.mTraversalScheduled = false;
            if (!createDragWindowAnimationLocked(this.mDragMode)) {
                resetDragWindowState();
                return;
            }
            UiThread.getHandler().removeCallbacks(this.dragWindowRun);
            updatePersonalAssistantAnimationState(1);
            if (sDebugfDetail) {
                Slog.v(TAG, "requestTraversal");
            }
            this.mWms.requestTraversal();
        }
    }

    private boolean createDragWindowAnimationLocked(int mode) {
        if (this.mColorDragWindowAnimation == null) {
            this.mColorDragWindowAnimation = new ColorDragWindowAnimation(this.mContext, this.mWms);
        }
        return this.mColorDragWindowAnimation.updateAnimationAndDragState(this.mPkg, this.mResId, this.mDragMode, this.mOptions);
    }

    public void removeDragWindowAnimation() {
        synchronized (mLock) {
            this.mColorDragWindowAnimation = null;
            mDragState = DRAG_STATE_HOLD;
            if (this.mMagnificationSpec.isNop()) {
                mDragState = DRAG_STATE_NORMAL;
            }
            updatePersonalAssistantAnimationState(0);
        }
        this.mWms.getDefaultDisplayContentLocked().getInputMonitor().updateInputWindowsLw(true);
        if (sDebugfDetail) {
            Slog.v(TAG, "removeDragWindowAnimation");
        }
    }

    public void resetDragWindowState() {
        mDragState = DRAG_STATE_NORMAL;
        this.mDragMode = -1;
    }

    public boolean canMagnificationSpec(WindowState win) {
        MagnificationSpec magnificationSpec = this.mMagnificationSpec;
        if (magnificationSpec == null || magnificationSpec.isNop()) {
            return false;
        }
        String titleName = win.mAttrs.getTitle().toString();
        if (!canMagnifyWindow(win.mAttrs.type) || titleName.contains(OPPO_RC) || titleName.contains(BREENNO_NAME) || titleName.contains(BREENNO_PANEL_NAME)) {
            return false;
        }
        return true;
    }

    private boolean canMagnifyWindow(int windowType) {
        if (windowType == 2019 || windowType == 2024 || windowType == 2027 || windowType == 2301 || windowType == 2302 || windowType == 2313 || windowType == 2314) {
            return false;
        }
        switch (windowType) {
            case 2011:
            case 2012:
            case 2013:
                return false;
            default:
                return true;
        }
    }

    public boolean inDragWindowing() {
        synchronized (mLock) {
            if (this.mMagnificationSpec == null || this.mMagnificationSpec.isNop()) {
                return false;
            }
            return true;
        }
    }

    public void recoveryState() {
        if (inDragWindowing() && this.mDefaultDisplayContent != null) {
            if (sDebugfDetail) {
                Slog.v(TAG, "recoveryState");
            }
            if (!this.mDefaultDisplayContent.okToAnimate()) {
                MagnificationSpec tmp = MagnificationSpec.obtain();
                tmp.clear();
                WindowManagerService windowManagerService = this.mWms;
                windowManagerService.applyMagnificationSpecLocked(windowManagerService.getDefaultDisplayContentLocked().getDisplayId(), tmp);
                getInstance().mMagnificationSpec.clear();
                removeDragWindowAnimation();
            }
        }
    }

    public void setBreenoState(String winName) {
        this.mBreeno = false;
        if (winName != null && winName.contains(BREENNO_NAME)) {
            this.mBreeno = true;
        }
    }

    public boolean isBreeno() {
        boolean z = this.mBreeno;
        if (!z) {
            return z;
        }
        this.mBreeno = false;
        return true;
    }

    private void updatePersonalAssistantAnimationState(int state) {
        Context context = this.mContext;
        if (context != null) {
            boolean result = Settings.Global.putInt(context.getContentResolver(), PERSONAL_ASSISTANT_ANIMATION_STATE, state);
            if (sDebugfDetail) {
                Slog.d(TAG, "update assistant animation state, result=" + result + ", state=" + state);
            }
        } else if (sDebugfDetail) {
            Slog.w(TAG, "Can not update assistant animation state, state=" + state);
        }
    }
}

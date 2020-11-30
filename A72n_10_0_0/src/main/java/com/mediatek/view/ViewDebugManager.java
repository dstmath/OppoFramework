package com.mediatek.view;

import android.os.Build;
import android.os.SystemProperties;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;

public class ViewDebugManager {
    public static boolean DBG = false;
    protected static int DBG_TIMEOUT_VALUE = 400;
    public static boolean DBG_TRANSP = false;
    public static boolean DEBUG_CHOREOGRAPHER_FRAMES = SystemProperties.getBoolean("debug.choreographer.frameslog", false);
    public static boolean DEBUG_CHOREOGRAPHER_JANK = SystemProperties.getBoolean("debug.choreographer.janklog", false);
    public static boolean DEBUG_CONFIGURATION = false;
    public static boolean DEBUG_DIALOG = false;
    public static boolean DEBUG_DRAW = false;
    public static final boolean DEBUG_ENG = "eng".equals(Build.TYPE);
    public static boolean DEBUG_FOCUS = false;
    public static boolean DEBUG_FPS = false;
    public static boolean DEBUG_HWUI = false;
    public static boolean DEBUG_IME_ANR = false;
    public static boolean DEBUG_IMF = false;
    public static boolean DEBUG_INPUT = false;
    public static boolean DEBUG_INPUT_RESIZE = false;
    public static boolean DEBUG_INPUT_STAGES = false;
    public static boolean DEBUG_INVALIDATE = false;
    public static boolean DEBUG_KEEP_SCREEN_ON = false;
    public static boolean DEBUG_KEY = false;
    public static boolean DEBUG_LAYOUT = false;
    public static boolean DEBUG_LIFECYCLE = false;
    public static boolean DEBUG_MET_TRACE = false;
    public static boolean DEBUG_MOTION = false;
    public static boolean DEBUG_ORIENTATION = false;
    public static boolean DEBUG_REQUESTLAYOUT = false;
    public static boolean DEBUG_SCHEDULETRAVERSALS = false;
    public static boolean DEBUG_SYSTRACE_DRAW = false;
    public static boolean DEBUG_SYSTRACE_LAYOUT = false;
    public static boolean DEBUG_SYSTRACE_MEASURE = false;
    public static boolean DEBUG_TOUCH = false;
    public static boolean DEBUG_TOUCHMODE = false;
    public static boolean DEBUG_TRACKBALL = false;
    public static boolean DEBUG_USER = false;
    public static final String INPUT_DISPATCH_STATE_DELIVER_EVENT = "4: Deliver input event";
    public static final String INPUT_DISPATCH_STATE_EARLY_POST_IME_STAGE = "8: Early post IME stage";
    public static final String INPUT_DISPATCH_STATE_ENQUEUE_EVENT = "2: Enqueue input event";
    public static final String INPUT_DISPATCH_STATE_FINISHED = "0: Finish handle input event";
    public static final String INPUT_DISPATCH_STATE_IME_STAGE = "7: IME stage";
    public static final String INPUT_DISPATCH_STATE_NATIVE_POST_IME_STAGE = "9: Native post IME stage";
    public static final String INPUT_DISPATCH_STATE_NATIVE_PRE_IME_STAGE = "5: Native pre IME stage";
    public static final String INPUT_DISPATCH_STATE_PROCESS_EVENT = "3 1: Process input event";
    public static final String INPUT_DISPATCH_STATE_SCHEDULE_EVENT = "3 2: Schedule process input event";
    public static final String INPUT_DISPATCH_STATE_STARTED = "1: Start event from input";
    public static final String INPUT_DISPATCH_STATE_SYNTHETC_INPUT_STAGE = "11: Synthetic input stage";
    public static final String INPUT_DISPATCH_STATE_VIEW_POST_IME_STAGE = "10: View Post IME stage";
    public static final String INPUT_DISPATCH_STATE_VIEW_PRE_IME_STAGE = "6: View pre IME stage";
    public static boolean LOCAL_LOGV = false;
    public static boolean debugHighFrameRateTouchLowLatency = SystemProperties.getBoolean("debug.mediatek.high_frame_rate_touch_to_display_low_lantency", false);
    private static Object lock = new Object();
    private static ViewDebugManager sInstance;

    public static ViewDebugManager getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    try {
                        sInstance = (ViewDebugManager) Class.forName("com.mediatek.view.impl.ViewDebugManagerImpl").getConstructor(new Class[0]).newInstance(new Object[0]);
                    } catch (Exception e) {
                        sInstance = new ViewDebugManager();
                    }
                }
            }
        }
        return sInstance;
    }

    public void debugKeyDispatch(View v, KeyEvent event) {
    }

    public void debugEventHandled(View v, InputEvent event, String handler) {
    }

    public void debugTouchDispatched(View v, MotionEvent event) {
    }

    public void warningParentToNull(View v) {
    }

    public void debugOnDrawDone(View v, long start) {
    }

    public long debugOnMeasureStart(View v, int widthMeasureSpec, int heightMeasureSpec, int oldWidthMeasureSpec, int oldHeightMeasureSpec) {
        return -1;
    }

    public void debugOnMeasureEnd(View v, long logTime) {
    }

    public void debugOnLayoutEnd(View v, long logTime) {
    }

    public void debugViewRemoved(View child, ViewGroup parent, Thread rootThread) {
    }

    public void debugViewGroupChildMeasure(View child, View parent, ViewGroup.MarginLayoutParams lp, int widthUsed, int heightUsed) {
    }

    public void debugViewGroupChildMeasure(View child, View parent, ViewGroup.LayoutParams lp, int widthUsed, int heightUsed) {
    }

    public void debugViewRootConstruct(String logTag, Object context, Object thread, Object chorgrapher, Object traversal, ViewRootImpl root) {
    }

    public void dumpInputDispatchingStatus(String logTag) {
    }

    public void debugInputStageDeliverd(Object stage, long time) {
    }

    public void debugInputEventStart(InputEvent event) {
    }

    public void debugInputEventFinished(String logTag, boolean handled, InputEvent event, ViewRootImpl root) {
    }

    public void debugTraveralDone(Object attachInfo, Object threadRender, boolean hwuiEnabled, ViewRootImpl root, boolean visable, boolean cancelDraw, String logTag) {
    }

    public void debugInputDispatchState(InputEvent event, String state) {
    }

    public boolean debugForceHWDraw(boolean hwDraw) {
        return hwDraw;
    }

    public int debugForceHWLayer(int hwLayer) {
        return hwLayer;
    }
}

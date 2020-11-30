package com.mediatek.view.impl;

import android.icu.text.SimpleDateFormat;
import android.os.SystemProperties;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import com.mediatek.view.ViewDebugManager;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ViewDebugManagerImpl extends ViewDebugManager {
    private static final String DATE_FORMAT_STRING = "yyyyMMdd_hhmmss";
    private static final int DBG_APP_DRAWING_MODE = SystemProperties.getInt("mtk_ro.mtk_dbg_app_drawing_mode", 0);
    private static final int DBG_APP_DRAWING_MODE_FORCE_HWLAYER = 4;
    private static final int DBG_APP_DRAWING_MODE_FORCE_HWUI = 1;
    private static final int DBG_APP_DRAWING_MODE_FORCE_SW = 2;
    private static final int DBG_APP_DRAWING_MODE_FORCE_SWLAYER = 8;
    private static final int DBG_APP_DRAWING_MODE_NOT_SET = 0;
    static final boolean DBG_APP_FAST_LAUNCH_ENHANCE;
    static final boolean DBG_APP_LAUNCH_ENHANCE = true;
    private static final boolean DBG_TRANSP = SystemProperties.getBoolean("mtk_d.view.transparentRegion", false);
    private static final int DEBUG_CHOREOGRAPHER_FRAMES_FLAG = 536870912;
    private static final int DEBUG_CHOREOGRAPHER_JANK_FLAG = 268435456;
    private static final int DEBUG_CONFIGURATION_FLAG = 256;
    private static final int DEBUG_DEFAULT_FLAG = 512;
    private static final int DEBUG_DIALOG_FLAG = 8;
    private static final int DEBUG_DRAW_FLAG = 2;
    private static final int DEBUG_ENABLE_ALL_FLAG = 1;
    private static final int DEBUG_FOCUS_FLAG = 16777216;
    private static final int DEBUG_FPS_FLAG = 1024;
    private static final int DEBUG_HWUI_FLAG = 2048;
    private static final int DEBUG_IME_ANR_FLAG = 32768;
    private static final int DEBUG_IMF_FLAG = 128;
    private static final int DEBUG_INPUT_FLAG = 4096;
    private static final int DEBUG_INPUT_RESIZE_FLAG = 16;
    private static final int DEBUG_INPUT_STAGES_FLAG = 4194304;
    private static final int DEBUG_INVALIDATE_FLAG = 262144;
    private static final int DEBUG_KEEP_SCREEN_ON_FLAG = 8388608;
    private static final int DEBUG_KEY_FLAG = 8192;
    private static final int DEBUG_LAYOUT_FLAG = 4;
    private static final int DEBUG_LIFECYCLE_FLAG = 65536;
    private static final int DEBUG_MET_TRACE_FLAG = 1073741824;
    private static final int DEBUG_MOTION_FLAG = 16384;
    private static final int DEBUG_ORIENTATION_FLAG = 32;
    private static final int DEBUG_REQUESTLAYOUT_FLAG = 131072;
    private static final int DEBUG_SCHEDULETRAVERSALS_FLAG = 524288;
    private static final int DEBUG_SYSTRACE_DRAW_FLAG = 33554432;
    private static final int DEBUG_SYSTRACE_LAYOUT_FLAG = 134217728;
    private static final int DEBUG_SYSTRACE_MEASURE_FLAG = 67108864;
    private static final int DEBUG_TOUCHMODE_FLAG = 1048576;
    private static final int DEBUG_TOUCH_FLAG = 2097152;
    private static final int DEBUG_TRACKBALL_FLAG = 64;
    private static final String DUMP_IMAGE_FORMAT = ".png";
    private static final String DUMP_IMAGE_PTAH = "/data/dump/";
    public static final int INPUT_TIMEOUT = 6000;
    private static final int LOG_DISABLED = 0;
    private static final int LOG_ENABLED = 1;
    private static final String LOG_PROPERTY_NAME = "mtk_d.viewroot.enable";
    private static final boolean USE_RENDER_THREAD = false;
    private static final String VIEWROOT_LOG_TAG = "ViewRoot";
    private static final String VIEW_LOG_TAG = "View";
    private static long sIdent = 0;
    private KeyEvent mCurrentKeyEvent;
    private MotionEvent mCurrentMotion;
    private long mIdent;
    private ConcurrentHashMap<Object, Long> mInputStageRecored = new ConcurrentHashMap<>();
    private long mKeyEventStartTime;
    private String mKeyEventStatus = "0: Finish handle input event";
    private long mMotionEventStartTime;
    private String mMotionEventStatus = "0: Finish handle input event";
    private KeyEvent mPreviousKeyEvent;
    private long mPreviousKeyEventFinishTime;
    private MotionEvent mPreviousMotion;
    private long mPreviousMotionEventFinishTime;

    static {
        boolean z = false;
        if (1 == SystemProperties.getInt("mtk_ro.mtk_perf_fast_start_win", 0)) {
            z = true;
        }
        DBG_APP_FAST_LAUNCH_ENHANCE = z;
    }

    public boolean debugForceHWDraw(boolean hwDraw) {
        int i = DBG_APP_DRAWING_MODE;
        if ((i & 1) == 1) {
            return DBG_APP_LAUNCH_ENHANCE;
        }
        if ((i & 2) == 2) {
            return false;
        }
        return hwDraw;
    }

    public int debugForceHWLayer(int hwLayer) {
        int i = DBG_APP_DRAWING_MODE;
        if ((i & 4) == 4) {
            return 2;
        }
        if ((i & 8) == 8) {
            return 1;
        }
        return hwLayer;
    }

    public void debugKeyDispatch(View v, KeyEvent event) {
        if (event.getAction() == 0) {
            Log.i(VIEW_LOG_TAG, "Key down dispatch to " + v + ", event = " + event);
        } else if (event.getAction() == 1) {
            Log.i(VIEW_LOG_TAG, "Key up dispatch to " + v + ", event = " + event);
        }
    }

    public void debugEventHandled(View v, InputEvent event, String handler) {
        Log.i(VIEW_LOG_TAG, "Event handle in " + v + ", event = " + event + ", handler = " + handler);
    }

    public void debugTouchDispatched(View v, MotionEvent event) {
        if (event.getAction() == 0) {
            Log.i(VIEW_LOG_TAG, "Touch down dispatch to " + v + ", event x = " + event.getX() + ",y = " + event.getY());
        } else if (event.getAction() == 1) {
            Log.i(VIEW_LOG_TAG, "Touch up dispatch to " + v + ", event x = " + event.getX() + ",y = " + event.getY());
        } else {
            Log.d(VIEW_LOG_TAG, "(View)dispatchTouchEvent: event action = " + MotionEvent.actionToString(event.getAction()) + ",x = " + event.getX() + ",y = " + event.getY() + ",this = " + v);
        }
    }

    public void warningParentToNull(View v) {
        if (ViewDebugManager.DEBUG_MOTION) {
            Log.d(VIEW_LOG_TAG, "assignParent to null: this = " + v + ", callstack = ", new Throwable());
        }
        Log.d(VIEW_LOG_TAG, "[Warning] assignParent to null: this = " + v);
    }

    public void debugOnDrawDone(View v, long start) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - start > ((long) DBG_TIMEOUT_VALUE)) {
            Log.d(VIEW_LOG_TAG, "[ANR Warning]onDraw time too long, this =" + v + "time =" + (nowTime - start) + " ms");
        }
        if (ViewDebugManager.DEBUG_DRAW) {
            Log.d(VIEW_LOG_TAG, "onDraw done, this =" + v + "time =" + (nowTime - start) + " ms");
        }
    }

    public long debugOnMeasureStart(View v, int widthMeasureSpec, int heightMeasureSpec, int oldWidthMeasureSpec, int oldHeightMeasureSpec) {
        if (ViewDebugManager.DEBUG_LAYOUT) {
            Log.d(VIEW_LOG_TAG, "view measure start, this = " + v + ", widthMeasureSpec = " + View.MeasureSpec.toString(widthMeasureSpec) + ", heightMeasureSpec = " + View.MeasureSpec.toString(heightMeasureSpec) + ", mOldWidthMeasureSpec = " + View.MeasureSpec.toString(oldWidthMeasureSpec) + ", mOldHeightMeasureSpec = " + View.MeasureSpec.toString(oldHeightMeasureSpec) + getViewLayoutProperties(v));
        }
        return System.currentTimeMillis();
    }

    public void debugOnMeasureEnd(View v, long logTime) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - logTime > ((long) ViewDebugManager.DBG_TIMEOUT_VALUE)) {
            Log.d(VIEW_LOG_TAG, "[ANR Warning]onMeasure time too long, this =" + v + "time =" + (nowTime - logTime) + " ms");
        }
        if (ViewDebugManager.DEBUG_LAYOUT) {
            Log.d(VIEW_LOG_TAG, "view onMeasure end (measure cache), this =" + v + ", mMeasuredWidth = " + v.getMeasuredWidth() + ", mMeasuredHeight = " + v.getMeasuredHeight() + ", time =" + (nowTime - logTime) + " ms");
        }
    }

    public void debugOnLayoutEnd(View v, long logTime) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - logTime > ((long) ViewDebugManager.DBG_TIMEOUT_VALUE)) {
            Log.d(VIEW_LOG_TAG, "[ANR Warning]onLayout time too long, this =" + v + "time =" + (nowTime - logTime) + " ms");
        }
        if (ViewDebugManager.DEBUG_LAYOUT) {
            Log.d(VIEW_LOG_TAG, "view layout end, this =" + v + ", time =" + (nowTime - logTime) + " ms");
        }
    }

    private String getViewLayoutProperties(View v) {
        StringBuilder out = new StringBuilder((int) DEBUG_IMF_FLAG);
        out.append(", Padding = {" + v.getPaddingLeft() + ", " + v.getPaddingTop() + ", " + v.getPaddingRight() + ", " + v.getPaddingBottom() + "}");
        if (v.getLayoutParams() == null) {
            out.append(", BAD! no layout params");
        } else {
            out.append(", " + v.getLayoutParams().debug(""));
        }
        return out.toString();
    }

    private int getCurrentLevel(View view) {
        int level = 0;
        ViewParent parent = view.getParent();
        while (parent != null && (parent instanceof View)) {
            level++;
            parent = ((View) parent).getParent();
        }
        return level;
    }

    private String sizeToString(int size) {
        if (size == -2) {
            return "wrap-content";
        }
        if (size == -1) {
            return "match-parent";
        }
        return String.valueOf(size);
    }

    public String debug(String output, ViewGroup.MarginLayoutParams params) {
        return output + "ViewGroup.MarginLayoutParams={ width=" + sizeToString(params.width) + ", height=" + sizeToString(params.height) + ", leftMargin=" + params.leftMargin + ", rightMargin=" + params.rightMargin + ", topMargin=" + params.topMargin + ", bottomMargin=" + params.bottomMargin + " }";
    }

    public void debugViewRemoved(View child, ViewGroup parent, Thread rootThread) {
        if (!(parent.getViewRootImpl() == null || rootThread == Thread.currentThread())) {
            Log.e(VIEW_LOG_TAG, "[Warning] remove view from parent not in UIThread: parent = " + parent + " view == " + child);
        }
        if (ViewDebugManager.DEBUG_LIFECYCLE) {
            Log.e(VIEW_LOG_TAG, "will remove view from parent " + parent + " view == " + child, new Throwable());
        }
    }

    public void debugViewGroupChildMeasure(View child, View parent, ViewGroup.MarginLayoutParams lp, int widthUsed, int heightUsed) {
        int level = getCurrentLevel(parent);
        Log.d(VIEW_LOG_TAG, "[ViewGroup][measureChildWithMargins] +" + level + " , child = " + child + ", child margin (L,R,T,B) = " + lp.leftMargin + "," + lp.rightMargin + "," + lp.topMargin + "," + lp.bottomMargin + ", widthUsed = " + widthUsed + ", heightUsed = " + heightUsed + ", parent padding (L,R,T,B) = " + parent.getPaddingLeft() + "," + parent.getPaddingRight() + "," + parent.getPaddingTop() + "," + parent.getPaddingBottom() + ", this = " + this);
    }

    public void debugViewGroupChildMeasure(View child, View parent, ViewGroup.LayoutParams lp, int widthUsed, int heightUsed) {
        int level = getCurrentLevel(parent);
        Log.d(VIEW_LOG_TAG, "[ViewGroup][measureChildWithMargins] +" + level + " , child = " + child + ", child params (width, height) = " + lp.width + "," + lp.height + ", widthUsed = " + widthUsed + ", heightUsed = " + heightUsed + ", parent padding (L,R,T,B) = " + parent.getPaddingLeft() + "," + parent.getPaddingRight() + "," + parent.getPaddingTop() + "," + parent.getPaddingBottom() + ", this = " + this);
    }

    public void debugViewRootConstruct(String logTag, Object context, Object thread, Object chorgrapher, Object traversal, ViewRootImpl root) {
        long j = sIdent;
        sIdent = 1 + j;
        root.mIdent = j;
        checkViewRootImplLogProperty();
        if (LOCAL_LOGV) {
            enableLog(logTag, DBG_APP_LAUNCH_ENHANCE);
        }
        if (DEBUG_LIFECYCLE) {
            Log.v(logTag, "ViewRootImpl construct: context = " + context + ", mThread = " + thread + ", mChoreographer = " + chorgrapher + ", mTraversalRunnable = " + traversal + ", this = " + root);
        }
    }

    /* access modifiers changed from: package-private */
    public void enableLog(String logTag, boolean enable) {
        Log.v(logTag, "enableLog: enable = " + enable);
        LOCAL_LOGV = enable;
        DEBUG_DRAW = enable;
        DEBUG_LAYOUT = enable;
        DEBUG_DIALOG = enable;
        DEBUG_INPUT_RESIZE = enable;
        DEBUG_ORIENTATION = enable;
        DEBUG_TRACKBALL = enable;
        DEBUG_IMF = enable;
        DEBUG_CONFIGURATION = enable;
        DEBUG_FPS = enable;
        DEBUG_INPUT = enable;
        DEBUG_IME_ANR = enable;
        DEBUG_LIFECYCLE = enable;
        DEBUG_REQUESTLAYOUT = enable;
        DEBUG_INVALIDATE = enable;
        DEBUG_SCHEDULETRAVERSALS = enable;
    }

    static void checkViewRootImplLogProperty() {
        String propString = SystemProperties.get(LOG_PROPERTY_NAME);
        boolean z = DBG_APP_LAUNCH_ENHANCE;
        ViewDebugManager.DEBUG_USER = DBG_APP_LAUNCH_ENHANCE;
        ViewDebugManager.DBG_TRANSP = DBG_TRANSP;
        if (propString != null && propString.length() > 0) {
            int logFilter = 0;
            try {
                logFilter = Integer.parseInt(propString, 16);
            } catch (NumberFormatException e) {
                Log.w(VIEWROOT_LOG_TAG, "Invalid format of propery string: " + propString);
            }
            Log.d(VIEWROOT_LOG_TAG, "checkViewRootImplLogProperty: propString = " + propString + ",logFilter = #" + Integer.toHexString(logFilter));
            boolean z2 = (logFilter & 1) == 1;
            ViewDebugManager.LOCAL_LOGV = z2;
            ViewRootImpl.LOCAL_LOGV = z2;
            boolean z3 = (logFilter & 2) == 2;
            ViewDebugManager.DEBUG_DRAW = z3;
            ViewRootImpl.DEBUG_DRAW = z3;
            boolean z4 = (logFilter & 4) == 4;
            ViewDebugManager.DEBUG_LAYOUT = z4;
            ViewRootImpl.DEBUG_LAYOUT = z4;
            boolean z5 = (logFilter & 8) == 8;
            ViewDebugManager.DEBUG_DIALOG = z5;
            ViewRootImpl.DEBUG_DIALOG = z5;
            boolean z6 = (logFilter & 16) == 16;
            ViewDebugManager.DEBUG_INPUT_RESIZE = z6;
            ViewRootImpl.DEBUG_INPUT_RESIZE = z6;
            boolean z7 = (logFilter & 32) == 32;
            ViewDebugManager.DEBUG_ORIENTATION = z7;
            ViewRootImpl.DEBUG_ORIENTATION = z7;
            boolean z8 = (logFilter & 64) == 64;
            ViewDebugManager.DEBUG_TRACKBALL = z8;
            ViewRootImpl.DEBUG_TRACKBALL = z8;
            boolean z9 = (logFilter & DEBUG_IMF_FLAG) == DEBUG_IMF_FLAG;
            ViewDebugManager.DEBUG_IMF = z9;
            ViewRootImpl.DEBUG_IMF = z9;
            boolean z10 = (logFilter & 256) == 256;
            ViewDebugManager.DEBUG_CONFIGURATION = z10;
            ViewRootImpl.DEBUG_CONFIGURATION = z10;
            boolean z11 = (logFilter & 512) == 512;
            ViewDebugManager.DBG = z11;
            ViewRootImpl.DBG = z11;
            boolean z12 = (logFilter & DEBUG_FPS_FLAG) == DEBUG_FPS_FLAG;
            ViewDebugManager.DEBUG_FPS = z12;
            ViewRootImpl.DEBUG_FPS = z12;
            boolean z13 = (logFilter & DEBUG_INPUT_STAGES_FLAG) == DEBUG_INPUT_STAGES_FLAG;
            ViewDebugManager.DEBUG_INPUT_STAGES = z13;
            ViewRootImpl.DEBUG_INPUT_STAGES = z13;
            boolean z14 = (logFilter & DEBUG_KEEP_SCREEN_ON_FLAG) == DEBUG_KEEP_SCREEN_ON_FLAG;
            ViewDebugManager.DEBUG_KEEP_SCREEN_ON = z14;
            ViewRootImpl.DEBUG_KEEP_SCREEN_ON = z14;
            ViewDebugManager.DEBUG_HWUI = (logFilter & DEBUG_HWUI_FLAG) == DEBUG_HWUI_FLAG;
            ViewDebugManager.DEBUG_INPUT = (logFilter & DEBUG_INPUT_FLAG) == DEBUG_INPUT_FLAG;
            ViewDebugManager.DEBUG_KEY = DEBUG_INPUT || (logFilter & DEBUG_KEY_FLAG) == DEBUG_KEY_FLAG;
            ViewDebugManager.DEBUG_MOTION = DEBUG_INPUT || (logFilter & DEBUG_MOTION_FLAG) == DEBUG_MOTION_FLAG;
            ViewDebugManager.DEBUG_IME_ANR = (DEBUG_IME_ANR_FLAG & logFilter) == DEBUG_IME_ANR_FLAG;
            ViewDebugManager.DEBUG_LIFECYCLE = (DEBUG_LIFECYCLE_FLAG & logFilter) == DEBUG_LIFECYCLE_FLAG;
            ViewDebugManager.DEBUG_REQUESTLAYOUT = (DEBUG_REQUESTLAYOUT_FLAG & logFilter) == DEBUG_REQUESTLAYOUT_FLAG;
            ViewDebugManager.DEBUG_INVALIDATE = (DEBUG_INVALIDATE_FLAG & logFilter) == DEBUG_INVALIDATE_FLAG;
            ViewDebugManager.DEBUG_SCHEDULETRAVERSALS = (DEBUG_SCHEDULETRAVERSALS_FLAG & logFilter) == DEBUG_SCHEDULETRAVERSALS_FLAG;
            ViewDebugManager.DEBUG_TOUCHMODE = (DEBUG_TOUCHMODE_FLAG & logFilter) == DEBUG_TOUCHMODE_FLAG;
            ViewDebugManager.DEBUG_TOUCH = (DEBUG_TOUCH_FLAG & logFilter) == DEBUG_TOUCH_FLAG;
            ViewDebugManager.DEBUG_FOCUS = (DEBUG_FOCUS_FLAG & logFilter) == DEBUG_FOCUS_FLAG;
            ViewDebugManager.DEBUG_SYSTRACE_MEASURE = (DEBUG_SYSTRACE_MEASURE_FLAG & logFilter) == DEBUG_SYSTRACE_MEASURE_FLAG;
            ViewDebugManager.DEBUG_SYSTRACE_LAYOUT = (DEBUG_SYSTRACE_LAYOUT_FLAG & logFilter) == DEBUG_SYSTRACE_LAYOUT_FLAG;
            ViewDebugManager.DEBUG_SYSTRACE_DRAW = (DEBUG_SYSTRACE_DRAW_FLAG & logFilter) == DEBUG_SYSTRACE_DRAW_FLAG;
            ViewDebugManager.DEBUG_CHOREOGRAPHER_JANK = (DEBUG_CHOREOGRAPHER_JANK_FLAG & logFilter) == DEBUG_CHOREOGRAPHER_JANK_FLAG;
            ViewDebugManager.DEBUG_CHOREOGRAPHER_FRAMES = (DEBUG_CHOREOGRAPHER_FRAMES_FLAG & logFilter) == DEBUG_CHOREOGRAPHER_FRAMES_FLAG;
            if ((DEBUG_MET_TRACE_FLAG & logFilter) != DEBUG_MET_TRACE_FLAG) {
                z = false;
            }
            ViewDebugManager.DEBUG_MET_TRACE = z;
        }
    }

    private static boolean checkAppLaunchTimeProperty() {
        if (1 == SystemProperties.getInt("persist.applaunchtime.enable", 0)) {
            return DBG_APP_LAUNCH_ENHANCE;
        }
        return false;
    }

    public void dumpInputDispatchingStatus(String logTag) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(this.mKeyEventStartTime);
        Date date2 = new Date(this.mPreviousKeyEventFinishTime);
        long dispatchTime = System.currentTimeMillis() - this.mKeyEventStartTime;
        if (this.mCurrentKeyEvent == null) {
            Log.i(logTag, "ANR Key Analyze: No Key event currently.");
            Log.i(logTag, "ANR Key Analyze: Previeous Event " + this.mPreviousKeyEvent + ",finish at " + simpleDateFormat.format(date2));
        } else {
            Log.i(logTag, "Dispatch " + this.mCurrentKeyEvent + " status is " + this.mKeyEventStatus + ",start at " + simpleDateFormat.format(date) + ", spent " + dispatchTime + "ms.");
        }
        if (this.mCurrentMotion == null) {
            date2.setTime(this.mPreviousMotionEventFinishTime);
            Log.i(logTag, "ANR Motion Analyze: No motion event currently.");
            Log.i(logTag, "ANR Motion Analyze: Previeous Event " + this.mPreviousMotion + ",finish at " + simpleDateFormat.format(date2));
        } else {
            date.setTime(this.mMotionEventStartTime);
            long dispatchTime2 = System.currentTimeMillis() - this.mMotionEventStartTime;
            Log.i(logTag, "Dispatch " + this.mCurrentMotion + " status is " + this.mMotionEventStatus + ",start at " + simpleDateFormat.format(date) + ", spent " + dispatchTime2 + "ms.");
        }
        dumpInputStageInfo(logTag, simpleDateFormat);
        clearInputStageInfo();
    }

    public void debugInputStageDeliverd(Object stage, long time) {
        this.mInputStageRecored.put(stage, Long.valueOf(time));
    }

    /* access modifiers changed from: package-private */
    public void clearInputStageInfo() {
        this.mInputStageRecored.clear();
    }

    /* access modifiers changed from: package-private */
    public void dumpInputStageInfo(String logTag, SimpleDateFormat sdf) {
        if (!this.mInputStageRecored.isEmpty()) {
            for (Object obj : this.mInputStageRecored.keySet()) {
                long dt = this.mInputStageRecored.get(obj).longValue();
                Date deliveredTime = new Date(dt);
                if (dt != 0) {
                    Log.v(logTag, "Input event delivered to " + obj + " at " + sdf.format(deliveredTime));
                }
            }
        }
    }

    public void debugInputEventStart(InputEvent event) {
        if (event instanceof KeyEvent) {
            this.mCurrentKeyEvent = (KeyEvent) event;
            this.mKeyEventStartTime = System.currentTimeMillis();
            this.mKeyEventStatus = "1: Start event from input";
            return;
        }
        this.mCurrentMotion = (MotionEvent) event;
        this.mMotionEventStartTime = System.currentTimeMillis();
        this.mMotionEventStatus = "1: Start event from input";
    }

    public void debugInputEventFinished(String logTag, boolean handled, InputEvent event, ViewRootImpl root) {
        long inputElapseTime;
        String stage;
        long currentTime = System.currentTimeMillis();
        if (event instanceof KeyEvent) {
            this.mPreviousKeyEvent = this.mCurrentKeyEvent;
            this.mPreviousKeyEventFinishTime = System.currentTimeMillis();
            this.mCurrentKeyEvent = null;
            stage = this.mKeyEventStatus;
            this.mKeyEventStatus = "0: Finish handle input event";
            inputElapseTime = currentTime - this.mKeyEventStartTime;
        } else {
            this.mPreviousMotion = this.mCurrentMotion;
            this.mPreviousMotionEventFinishTime = System.currentTimeMillis();
            this.mCurrentMotion = null;
            stage = this.mMotionEventStatus;
            this.mMotionEventStatus = "0: Finish handle input event";
            inputElapseTime = currentTime - this.mMotionEventStartTime;
        }
        if (inputElapseTime >= 6000) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date enqueueTime = new Date(currentTime - inputElapseTime);
            Log.v(logTag, "[ANR Warning]Input routeing takes more than 6000ms since " + simpleDateFormat.format(enqueueTime) + ", this = " + this);
            dumpInputStageInfo(logTag, simpleDateFormat);
        }
        clearInputStageInfo();
        if (!DEBUG_ENG && !DEBUG_INPUT && !DEBUG_KEY && !DEBUG_MOTION) {
            return;
        }
        if (event instanceof MotionEvent) {
            Log.v(logTag, "finishInputEvent: handled = " + handled + ",event action = " + MotionEvent.actionToString(((MotionEvent) event).getAction()) + ",x = " + ((MotionEvent) event).getX() + ",y = " + ((MotionEvent) event).getY() + ", stage = " + stage);
            return;
        }
        Log.v(logTag, "finishInputEvent: handled = " + handled + ",event = " + event + ", stage = " + stage);
    }

    public void debugInputDispatchState(InputEvent event, String state) {
        if (event instanceof KeyEvent) {
            setKeyDispatchState(state);
        } else {
            setMotionDispatchState(state);
        }
    }

    /* access modifiers changed from: package-private */
    public void setKeyDispatchState(String state) {
        this.mKeyEventStatus = state;
    }

    /* access modifiers changed from: package-private */
    public void setMotionDispatchState(String state) {
        this.mMotionEventStatus = state;
    }

    public void debugTraveralDone(Object attachInfo, Object threadRender, boolean hwuiEnabled, ViewRootImpl root, boolean visable, boolean cancelDraw, String logTag) {
        long frameCount = -999;
        if (!(attachInfo == null || threadRender == null || !hwuiEnabled)) {
            frameCount = (long) root.mFrame;
        }
        Log.v(logTag, "ViewRoot performTraversals and draw- : frame#" + frameCount + ", isViewVisible = " + visable + " (cancelDraw = " + cancelDraw + ")");
        root.mFrame = root.mFrame + 1;
    }
}

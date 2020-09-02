package android.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Point;
import android.os.SystemClock;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

@Deprecated
public class TouchUtils {
    @Deprecated
    public static void dragQuarterScreenDown(ActivityInstrumentationTestCase test) {
        dragQuarterScreenDown(test, test.getActivity());
    }

    public static void dragQuarterScreenDown(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float x = ((float) size.x) / 2.0f;
        drag(test, x, x, ((float) size.y) * 0.5f, ((float) size.y) * 0.75f, 4);
    }

    @Deprecated
    public static void dragQuarterScreenUp(ActivityInstrumentationTestCase test) {
        dragQuarterScreenUp(test, test.getActivity());
    }

    public static void dragQuarterScreenUp(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float x = ((float) size.x) / 2.0f;
        drag(test, x, x, ((float) size.y) * 0.5f, ((float) size.y) * 0.25f, 4);
    }

    @Deprecated
    public static void scrollToBottom(ActivityInstrumentationTestCase test, ViewGroup v) {
        scrollToBottom(test, test.getActivity(), v);
    }

    public static void scrollToBottom(InstrumentationTestCase test, Activity activity, ViewGroup v) {
        ViewStateSnapshot next = new ViewStateSnapshot(v);
        do {
            dragQuarterScreenUp(test, activity);
            next = new ViewStateSnapshot(v);
        } while (!next.equals(next));
    }

    @Deprecated
    public static void scrollToTop(ActivityInstrumentationTestCase test, ViewGroup v) {
        scrollToTop(test, test.getActivity(), v);
    }

    public static void scrollToTop(InstrumentationTestCase test, Activity activity, ViewGroup v) {
        ViewStateSnapshot next = new ViewStateSnapshot(v);
        do {
            dragQuarterScreenDown(test, activity);
            next = new ViewStateSnapshot(v);
        } while (!next.equals(next));
    }

    @Deprecated
    public static void dragViewToBottom(ActivityInstrumentationTestCase test, View v) {
        dragViewToBottom(test, test.getActivity(), v, 4);
    }

    public static void dragViewToBottom(InstrumentationTestCase test, Activity activity, View v) {
        dragViewToBottom(test, activity, v, 4);
    }

    @Deprecated
    public static void dragViewToBottom(ActivityInstrumentationTestCase test, View v, int stepCount) {
        dragViewToBottom(test, test.getActivity(), v, stepCount);
    }

    public static void dragViewToBottom(InstrumentationTestCase test, Activity activity, View v, int stepCount) {
        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        float x = ((float) xy[0]) + (((float) viewWidth) / 2.0f);
        drag(test, x, x, ((float) xy[1]) + (((float) viewHeight) / 2.0f), (float) (screenHeight - 1), stepCount);
    }

    public static void tapView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        float x = ((float) xy[0]) + (((float) viewWidth) / 2.0f);
        float y = ((float) xy[1]) + (((float) viewHeight) / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, x, y, 0));
        inst.waitForIdleSync();
        long eventTime = SystemClock.uptimeMillis();
        int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, 2, x + (((float) touchSlop) / 2.0f), y + (((float) touchSlop) / 2.0f), 0));
        inst.waitForIdleSync();
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, x, y, 0));
        inst.waitForIdleSync();
    }

    public static void touchAndCancelView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        float x = ((float) xy[0]) + (((float) viewWidth) / 2.0f);
        float y = ((float) xy[1]) + (((float) viewHeight) / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, x, y, 0));
        inst.waitForIdleSync();
        long eventTime = SystemClock.uptimeMillis();
        int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, 3, x + (((float) touchSlop) / 2.0f), y + (((float) touchSlop) / 2.0f), 0));
        inst.waitForIdleSync();
    }

    public static void clickView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        float x = (((float) viewWidth) / 2.0f) + ((float) xy[0]);
        float y = ((float) xy[1]) + (((float) viewHeight) / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, x, y, 0));
        inst.waitForIdleSync();
        long eventTime = SystemClock.uptimeMillis();
        int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, 2, x + (((float) touchSlop) / 2.0f), y + (((float) touchSlop) / 2.0f), 0));
        inst.waitForIdleSync();
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, x, y, 0));
        inst.waitForIdleSync();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.longClickView(android.test.InstrumentationTestCase, android.view.View):void
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View]
     candidates:
      android.test.TouchUtils.longClickView(android.test.ActivityInstrumentationTestCase, android.view.View):void
      android.test.TouchUtils.longClickView(android.test.InstrumentationTestCase, android.view.View):void */
    @Deprecated
    public static void longClickView(ActivityInstrumentationTestCase test, View v) {
        longClickView((InstrumentationTestCase) test, v);
    }

    public static void longClickView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        float x = (((float) viewWidth) / 2.0f) + ((float) xy[0]);
        float y = ((float) xy[1]) + (((float) viewHeight) / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, x, y, 0));
        inst.waitForIdleSync();
        long eventTime = SystemClock.uptimeMillis();
        int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        inst.sendPointerSync(MotionEvent.obtain(downTime, eventTime, 2, x + ((float) (touchSlop / 2)), y + ((float) (touchSlop / 2)), 0));
        inst.waitForIdleSync();
        try {
            Thread.sleep((long) (((float) ViewConfiguration.getLongPressTimeout()) * 1.5f));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, x, y, 0));
        inst.waitForIdleSync();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.dragViewToTop(android.test.InstrumentationTestCase, android.view.View, int):void
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View, int]
     candidates:
      android.test.TouchUtils.dragViewToTop(android.test.ActivityInstrumentationTestCase, android.view.View, int):void
      android.test.TouchUtils.dragViewToTop(android.test.InstrumentationTestCase, android.view.View, int):void */
    @Deprecated
    public static void dragViewToTop(ActivityInstrumentationTestCase test, View v) {
        dragViewToTop((InstrumentationTestCase) test, v, 4);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.dragViewToTop(android.test.InstrumentationTestCase, android.view.View, int):void
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View, int]
     candidates:
      android.test.TouchUtils.dragViewToTop(android.test.ActivityInstrumentationTestCase, android.view.View, int):void
      android.test.TouchUtils.dragViewToTop(android.test.InstrumentationTestCase, android.view.View, int):void */
    @Deprecated
    public static void dragViewToTop(ActivityInstrumentationTestCase test, View v, int stepCount) {
        dragViewToTop((InstrumentationTestCase) test, v, stepCount);
    }

    public static void dragViewToTop(InstrumentationTestCase test, View v) {
        dragViewToTop(test, v, 4);
    }

    public static void dragViewToTop(InstrumentationTestCase test, View v, int stepCount) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        float x = ((float) xy[0]) + (((float) viewWidth) / 2.0f);
        drag(test, x, x, ((float) xy[1]) + (((float) viewHeight) / 2.0f), 0.0f, stepCount);
    }

    private static void getStartLocation(View v, int gravity, int[] xy) {
        v.getLocationOnScreen(xy);
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();
        int i = gravity & 112;
        if (i == 16) {
            xy[1] = xy[1] + (viewHeight / 2);
        } else if (i != 48 && i == 80) {
            xy[1] = xy[1] + (viewHeight - 1);
        }
        int i2 = gravity & 7;
        if (i2 == 1) {
            xy[0] = xy[0] + (viewWidth / 2);
        } else if (i2 != 3 && i2 == 5) {
            xy[0] = xy[0] + (viewWidth - 1);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.dragViewBy(android.test.InstrumentationTestCase, android.view.View, int, int, int):int
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View, int, int, int]
     candidates:
      android.test.TouchUtils.dragViewBy(android.test.ActivityInstrumentationTestCase, android.view.View, int, int, int):int
      android.test.TouchUtils.dragViewBy(android.test.InstrumentationTestCase, android.view.View, int, int, int):int */
    @Deprecated
    public static int dragViewBy(ActivityInstrumentationTestCase test, View v, int gravity, int deltaX, int deltaY) {
        return dragViewBy((InstrumentationTestCase) test, v, gravity, deltaX, deltaY);
    }

    @Deprecated
    public static int dragViewBy(InstrumentationTestCase test, View v, int gravity, int deltaX, int deltaY) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        int fromX = xy[0];
        int fromY = xy[1];
        int distance = (int) Math.hypot((double) deltaX, (double) deltaY);
        drag(test, (float) fromX, (float) (fromX + deltaX), (float) fromY, (float) (fromY + deltaY), distance);
        return distance;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.dragViewTo(android.test.InstrumentationTestCase, android.view.View, int, int, int):int
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View, int, int, int]
     candidates:
      android.test.TouchUtils.dragViewTo(android.test.ActivityInstrumentationTestCase, android.view.View, int, int, int):int
      android.test.TouchUtils.dragViewTo(android.test.InstrumentationTestCase, android.view.View, int, int, int):int */
    @Deprecated
    public static int dragViewTo(ActivityInstrumentationTestCase test, View v, int gravity, int toX, int toY) {
        return dragViewTo((InstrumentationTestCase) test, v, gravity, toX, toY);
    }

    public static int dragViewTo(InstrumentationTestCase test, View v, int gravity, int toX, int toY) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        int fromX = xy[0];
        int fromY = xy[1];
        int distance = (int) Math.hypot((double) (fromX - toX), (double) (fromY - toY));
        drag(test, (float) fromX, (float) toX, (float) fromY, (float) toY, distance);
        return distance;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.dragViewToX(android.test.InstrumentationTestCase, android.view.View, int, int):int
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View, int, int]
     candidates:
      android.test.TouchUtils.dragViewToX(android.test.ActivityInstrumentationTestCase, android.view.View, int, int):int
      android.test.TouchUtils.dragViewToX(android.test.InstrumentationTestCase, android.view.View, int, int):int */
    @Deprecated
    public static int dragViewToX(ActivityInstrumentationTestCase test, View v, int gravity, int toX) {
        return dragViewToX((InstrumentationTestCase) test, v, gravity, toX);
    }

    public static int dragViewToX(InstrumentationTestCase test, View v, int gravity, int toX) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        int fromX = xy[0];
        int fromY = xy[1];
        int deltaX = fromX - toX;
        drag(test, (float) fromX, (float) toX, (float) fromY, (float) fromY, deltaX);
        return deltaX;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.dragViewToY(android.test.InstrumentationTestCase, android.view.View, int, int):int
     arg types: [android.test.ActivityInstrumentationTestCase, android.view.View, int, int]
     candidates:
      android.test.TouchUtils.dragViewToY(android.test.ActivityInstrumentationTestCase, android.view.View, int, int):int
      android.test.TouchUtils.dragViewToY(android.test.InstrumentationTestCase, android.view.View, int, int):int */
    @Deprecated
    public static int dragViewToY(ActivityInstrumentationTestCase test, View v, int gravity, int toY) {
        return dragViewToY((InstrumentationTestCase) test, v, gravity, toY);
    }

    public static int dragViewToY(InstrumentationTestCase test, View v, int gravity, int toY) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        int fromX = xy[0];
        int fromY = xy[1];
        int deltaY = fromY - toY;
        drag(test, (float) fromX, (float) fromX, (float) fromY, (float) toY, deltaY);
        return deltaY;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.test.TouchUtils.drag(android.test.InstrumentationTestCase, float, float, float, float, int):void
     arg types: [android.test.ActivityInstrumentationTestCase, float, float, float, float, int]
     candidates:
      android.test.TouchUtils.drag(android.test.ActivityInstrumentationTestCase, float, float, float, float, int):void
      android.test.TouchUtils.drag(android.test.InstrumentationTestCase, float, float, float, float, int):void */
    @Deprecated
    public static void drag(ActivityInstrumentationTestCase test, float fromX, float toX, float fromY, float toY, int stepCount) {
        drag((InstrumentationTestCase) test, fromX, toX, fromY, toY, stepCount);
    }

    public static void drag(InstrumentationTestCase test, float fromX, float toX, float fromY, float toY, int stepCount) {
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        float yStep = (toY - fromY) / ((float) stepCount);
        float xStep = (toX - fromX) / ((float) stepCount);
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, fromX, fromY, 0));
        float y = fromY;
        float x = fromX;
        for (int i = 0; i < stepCount; i++) {
            y += yStep;
            x += xStep;
            inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 2, x, y, 0));
        }
        inst.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, x, y, 0));
        inst.waitForIdleSync();
    }

    private static class ViewStateSnapshot {
        final int mChildCount;
        final View mFirst;
        final int mFirstTop;
        final View mLast;
        final int mLastBottom;

        private ViewStateSnapshot(ViewGroup viewGroup) {
            this.mChildCount = viewGroup.getChildCount();
            if (this.mChildCount == 0) {
                this.mLast = null;
                this.mFirst = null;
                this.mLastBottom = Integer.MIN_VALUE;
                this.mFirstTop = Integer.MIN_VALUE;
                return;
            }
            this.mFirst = viewGroup.getChildAt(0);
            this.mLast = viewGroup.getChildAt(this.mChildCount - 1);
            this.mFirstTop = this.mFirst.getTop();
            this.mLastBottom = this.mLast.getBottom();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ViewStateSnapshot that = (ViewStateSnapshot) o;
            if (this.mFirstTop == that.mFirstTop && this.mLastBottom == that.mLastBottom && this.mFirst == that.mFirst && this.mLast == that.mLast && this.mChildCount == that.mChildCount) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            View view = this.mFirst;
            int i = 0;
            int hashCode = (view != null ? view.hashCode() : 0) * 31;
            View view2 = this.mLast;
            if (view2 != null) {
                i = view2.hashCode();
            }
            return ((((((hashCode + i) * 31) + this.mFirstTop) * 31) + this.mLastBottom) * 31) + this.mChildCount;
        }
    }
}

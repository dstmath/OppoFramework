package com.android.server.accessibility;

import android.util.MathUtils;
import android.view.MotionEvent;
import com.android.server.display.OppoBrightUtils;

final class GestureUtils {
    private GestureUtils() {
    }

    public static boolean isTap(MotionEvent down, MotionEvent up, int tapTimeSlop, int tapDistanceSlop, int actionIndex) {
        return eventsWithinTimeAndDistanceSlop(down, up, tapTimeSlop, tapDistanceSlop, actionIndex);
    }

    public static boolean isMultiTap(MotionEvent firstUp, MotionEvent secondUp, int multiTapTimeSlop, int multiTapDistanceSlop, int actionIndex) {
        return eventsWithinTimeAndDistanceSlop(firstUp, secondUp, multiTapTimeSlop, multiTapDistanceSlop, actionIndex);
    }

    private static boolean eventsWithinTimeAndDistanceSlop(MotionEvent first, MotionEvent second, int timeout, int distance, int actionIndex) {
        if (!isTimedOut(first, second, timeout) && computeDistance(first, second, actionIndex) < ((double) distance)) {
            return true;
        }
        return false;
    }

    public static double computeDistance(MotionEvent first, MotionEvent second, int pointerIndex) {
        return (double) MathUtils.dist(first.getX(pointerIndex), first.getY(pointerIndex), second.getX(pointerIndex), second.getY(pointerIndex));
    }

    public static boolean isTimedOut(MotionEvent firstUp, MotionEvent secondUp, int timeout) {
        return secondUp.getEventTime() - firstUp.getEventTime() >= ((long) timeout);
    }

    public static boolean isSamePointerContext(MotionEvent first, MotionEvent second) {
        if (first.getPointerIdBits() == second.getPointerIdBits() && first.getPointerId(first.getActionIndex()) == second.getPointerId(second.getActionIndex())) {
            return true;
        }
        return false;
    }

    public static boolean isDraggingGesture(float firstPtrDownX, float firstPtrDownY, float secondPtrDownX, float secondPtrDownY, float firstPtrX, float firstPtrY, float secondPtrX, float secondPtrY, float maxDraggingAngleCos) {
        float firstDeltaX = firstPtrX - firstPtrDownX;
        float firstDeltaY = firstPtrY - firstPtrDownY;
        if (firstDeltaX == OppoBrightUtils.MIN_LUX_LIMITI && firstDeltaY == OppoBrightUtils.MIN_LUX_LIMITI) {
            return true;
        }
        float firstMagnitude = (float) Math.hypot((double) firstDeltaX, (double) firstDeltaY);
        float firstXNormalized = firstMagnitude > OppoBrightUtils.MIN_LUX_LIMITI ? firstDeltaX / firstMagnitude : firstDeltaX;
        float firstYNormalized = firstMagnitude > OppoBrightUtils.MIN_LUX_LIMITI ? firstDeltaY / firstMagnitude : firstDeltaY;
        float secondDeltaX = secondPtrX - secondPtrDownX;
        float secondDeltaY = secondPtrY - secondPtrDownY;
        if (secondDeltaX == OppoBrightUtils.MIN_LUX_LIMITI && secondDeltaY == OppoBrightUtils.MIN_LUX_LIMITI) {
            return true;
        }
        float secondMagnitude = (float) Math.hypot((double) secondDeltaX, (double) secondDeltaY);
        if ((firstXNormalized * (secondMagnitude > OppoBrightUtils.MIN_LUX_LIMITI ? secondDeltaX / secondMagnitude : secondDeltaX)) + (firstYNormalized * (secondMagnitude > OppoBrightUtils.MIN_LUX_LIMITI ? secondDeltaY / secondMagnitude : secondDeltaY)) < maxDraggingAngleCos) {
            return false;
        }
        return true;
    }
}

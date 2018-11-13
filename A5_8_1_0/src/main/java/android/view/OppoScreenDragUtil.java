package android.view;

import android.os.SystemProperties;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowState;

public class OppoScreenDragUtil {
    public static int DRAG_STATE_HOLD = 1;
    public static int DRAG_STATE_NORMAL = 0;
    public static int DRAG_STATE_OFFSET = 2;
    private static final int FINISH_HANDLED = 1;
    private static final int FORWARD = 0;
    private static final String PERSIST_KEY = "persist.sys.oppo.screendrag";
    private static final String PERSIST_KEY_METRICS = "persist.sys.oppo.displaymetrics";
    private static final String PERSIST_KEY_STATE = "persist.sys.oppo.dragstate";
    private static String SPLIT_PROP = ",";

    public static void setScreenDragState(int dragState) {
        SystemProperties.set(PERSIST_KEY, String.valueOf(dragState) + ",0,0,0");
    }

    public static int getScreenDragState() {
        return Integer.parseInt(SystemProperties.get(PERSIST_KEY, "0,0,0,0").split(SPLIT_PROP)[0]);
    }

    public static int getOffsetX() {
        return Integer.parseInt(SystemProperties.get(PERSIST_KEY, "0,0,0,0").split(SPLIT_PROP)[1]);
    }

    public static int getOffsetY() {
        return Integer.parseInt(SystemProperties.get(PERSIST_KEY, "0,0,0,0").split(SPLIT_PROP)[2]);
    }

    public static float getScale() {
        return Float.parseFloat(SystemProperties.get(PERSIST_KEY, "0,0,0,0").split(SPLIT_PROP)[3]);
    }

    public static int getWidth() {
        return Integer.parseInt(SystemProperties.get(PERSIST_KEY_METRICS, "0,0").split(SPLIT_PROP)[0]);
    }

    public static int getHeight() {
        return Integer.parseInt(SystemProperties.get(PERSIST_KEY_METRICS, "0,0").split(SPLIT_PROP)[1]);
    }

    public static boolean isNormalState() {
        return DRAG_STATE_NORMAL == getScreenDragState();
    }

    public static boolean isDragState() {
        int state = getScreenDragState();
        if (DRAG_STATE_HOLD == state || DRAG_STATE_OFFSET == state) {
            return true;
        }
        return false;
    }

    public static boolean isHoldState() {
        return DRAG_STATE_HOLD == getScreenDragState();
    }

    public static boolean isOffsetState() {
        return DRAG_STATE_OFFSET == getScreenDragState();
    }

    public static boolean isShowWallpaper(WindowState windowState) {
        return isDragState() && windowState != null && windowState.getAttrs().type < 99;
    }

    public static boolean isWallpaperWin(WindowState windowState) {
        return isDragState() && windowState != null && windowState.getAttrs().type == LayoutParams.TYPE_WALLPAPER;
    }

    public static void resetState() {
        setScreenDragState(DRAG_STATE_NORMAL);
        SystemProperties.set(PERSIST_KEY_STATE, "0");
        SystemProperties.set(PERSIST_KEY_METRICS, "0,0");
    }

    public static float getOffsetPosX(float x) {
        if (getOffsetX() == 0) {
            return x / getScale();
        }
        return (x - (((float) getWidth()) * (1.0f - getScale()))) / getScale();
    }

    public static float getOffsetPosY(float y) {
        return (y - (((float) getHeight()) * (1.0f - getScale()))) / getScale();
    }

    public static float getOffsetPosXScale(float x, float scale) {
        if (getOffsetX() == 0) {
            return x / getScale();
        }
        return (x - ((((float) getWidth()) / scale) * (1.0f - getScale()))) / getScale();
    }

    public static float getOffsetPosYScale(float y, float scale) {
        return (y - ((((float) getHeight()) / scale) * (1.0f - getScale()))) / getScale();
    }

    public static void setEventLocationScale(MotionEvent event, float scale) {
        MotionEvent newEvent = event;
        event.setLocation(getOffsetPosXScale(event.getX() - event.getXOffset(), scale) + event.getXOffset(), getOffsetPosYScale(event.getY() - event.getYOffset(), scale) + event.getYOffset());
    }

    public static void setEventLocation(MotionEvent event) {
        MotionEvent newEvent = event;
        event.setLocation(getOffsetPosX(event.getX() - event.getXOffset()) + event.getXOffset(), getOffsetPosY(event.getY() - event.getYOffset()) + event.getYOffset());
    }

    public static int screenOffsetDeliverPointer(MotionEvent event, View view) {
        MotionEvent newEvent = event;
        event.setLocation(getOffsetPosX(event.getX() - event.getXOffset()) + event.getXOffset(), getOffsetPosY(event.getY() - event.getYOffset()) + event.getYOffset());
        if (view.dispatchPointerEvent(event)) {
            return 1;
        }
        return 0;
    }
}

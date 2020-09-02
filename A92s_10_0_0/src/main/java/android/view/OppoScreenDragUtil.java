package android.view;

import android.graphics.Rect;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.telephony.SmsManager;

public class OppoScreenDragUtil {
    public static int DRAG_STATE_HOLD = 1;
    public static int DRAG_STATE_NORMAL = 0;
    public static int DRAG_STATE_OFFSET = 2;
    private static final int FINISH_HANDLED = 1;
    private static final int FORWARD = 0;
    private static final float GLOABL_SCALE_COMPAT_APP = 1.333333f;
    private static final String PERSIST_KEY = "persist.sys.oppo.screendrag";
    private static final String PERSIST_KEY_METRICS = "persist.sys.oppo.displaymetrics";
    private static final String PERSIST_KEY_STATE = "persist.sys.oppo.dragstate";
    private static String SPLIT_PROP = SmsManager.REGEX_PREFIX_DELIMITER;

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
        return DRAG_STATE_HOLD == state || DRAG_STATE_OFFSET == state;
    }

    public static boolean isHoldState() {
        return DRAG_STATE_HOLD == getScreenDragState();
    }

    public static boolean isOffsetState() {
        return DRAG_STATE_OFFSET == getScreenDragState();
    }

    public static void resetState() {
        setScreenDragState(DRAG_STATE_NORMAL);
        SystemProperties.set(PERSIST_KEY_STATE, WifiEnterpriseConfig.ENGINE_DISABLE);
        SystemProperties.set(PERSIST_KEY_METRICS, "0,0");
    }

    public static float getOffsetPosX(float x) {
        if (getOffsetX() == 0) {
            return x;
        }
        return x - (((float) getOffsetX()) / getScale());
    }

    public static float getOffsetPosY(float y) {
        return y - (((float) getOffsetY()) / getScale());
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
        event.setLocation(getOffsetPosXScale(event.getX() - getXOffset(event), scale) + getXOffset(event), getOffsetPosYScale(event.getY() - getYOffset(event), scale) + getYOffset(event));
    }

    public static void setEventLocation(MotionEvent event) {
        event.setLocation(getOffsetPosX(event.getX() - getXOffset(event)) + getXOffset(event), getOffsetPosY(event.getY() - getYOffset(event)) + getYOffset(event));
    }

    public static int screenOffsetDeliverPointer(MotionEvent event, View view) {
        event.setLocation(getOffsetPosX(event.getX() - getXOffset(event)) + getXOffset(event), getOffsetPosY(event.getY() - getYOffset(event)) + getYOffset(event));
        if (view.dispatchPointerEvent(event)) {
            return 1;
        }
        return 0;
    }

    public static void scaleScreenshotIfNeeded(Rect sourceCrop) {
        if (isOffsetState()) {
            float scale = getScale();
            int w = getWidth();
            int h = getHeight();
            int sw = sourceCrop.width();
            int sh = sourceCrop.height();
            if (getOffsetX() > 0) {
                sourceCrop.left = (int) (((1.0f - scale) * ((float) w)) + (((float) sourceCrop.left) * scale));
                sourceCrop.top = (int) (((1.0f - scale) * ((float) h)) + (((float) sourceCrop.top) * scale));
                sourceCrop.right = sourceCrop.left + ((int) (((float) sw) * scale));
                sourceCrop.bottom = sourceCrop.top + ((int) (((float) sh) * scale));
                return;
            }
            sourceCrop.left = (int) (((float) sourceCrop.left) * scale);
            sourceCrop.top = (int) (((1.0f - scale) * ((float) h)) + (((float) sourceCrop.top) * scale));
            sourceCrop.right = sourceCrop.left + ((int) (((float) sw) * scale));
            sourceCrop.bottom = sourceCrop.top + ((int) (((float) sh) * scale));
        }
    }

    public static int adjustRawXForResolution(int x) {
        int offSetX = getOffsetX();
        if (offSetX == 0) {
            return x;
        }
        float scale = getScale();
        return (int) (((float) x) + (((1.0f / scale) - (1.0f / (GLOABL_SCALE_COMPAT_APP * scale))) * ((float) offSetX)));
    }

    public static int adjustRawYForResolution(int y) {
        int offSetY = getOffsetY();
        float scale = getScale();
        if (scale < 0.7f) {
            return y;
        }
        return (int) (((float) y) + (((1.0f / scale) - (1.0f / (GLOABL_SCALE_COMPAT_APP * scale))) * ((float) offSetY)));
    }

    private static float getXOffset(MotionEvent event) {
        long nativePtr = OppoMirrirMotionEvent.mNativePtr.get(event);
        return OppoMirrirMotionEvent.nativeGetXOffset.call(Long.valueOf(nativePtr)).floatValue();
    }

    private static float getYOffset(MotionEvent event) {
        return OppoMirrirMotionEvent.nativeGetYOffset.call(Long.valueOf(OppoMirrirMotionEvent.mNativePtr.get(event))).floatValue();
    }
}

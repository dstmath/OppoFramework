package com.android.server.wm;

import android.graphics.Region;
import android.util.Slog;

public class ColorZoomWindowDebugUtil {
    public static final String KEY_TPCONTROLLER_HANDLETAPOUTSIDETASK = "TaskPositioningController: handleTapOutsideTask";
    public static final String KEY_TTPEVENTLISTENER_ONPOINTEREVENT = "TaskTapPointerEventListener: onPointerEvent";
    public static final String KEY_ZOOMWINDOWSERVICE_HANDLETAPOUTSIDETASK = "ColorZoomWindowManagerService: handleTapOutsideTask";
    private static final String TAG = "ColorZoomWindowDebugUtil";

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0062  */
    public static void debugLogUtil(String key, Object... vars) {
        char c;
        int hashCode = key.hashCode();
        if (hashCode != -1014578287) {
            if (hashCode != -890179517) {
                if (hashCode == 966743941 && key.equals(KEY_TPCONTROLLER_HANDLETAPOUTSIDETASK)) {
                    c = 1;
                    if (c == 0) {
                        if (c != 1) {
                            if (c != 2) {
                                Slog.w(TAG, "Unknown key for zoom window dynamic debug: " + key);
                                return;
                            } else if (ColorZoomWindowDebugConfig.DEBUG_LIFE_CYCLE) {
                                logZoomWindowServiceHandleTapOutSideTask(key, vars);
                                return;
                            } else {
                                return;
                            }
                        } else if (ColorZoomWindowDebugConfig.DEBUG_LIFE_CYCLE) {
                            logTPControllerHandleTapOutSideTask(key, vars);
                            return;
                        } else {
                            return;
                        }
                    } else if (ColorZoomWindowDebugConfig.DEBUG_LIFE_CYCLE) {
                        logTTPEventListenerOnPointerEvent(key, vars);
                        return;
                    } else {
                        return;
                    }
                }
            } else if (key.equals(KEY_ZOOMWINDOWSERVICE_HANDLETAPOUTSIDETASK)) {
                c = 2;
                if (c == 0) {
                }
            }
        } else if (key.equals(KEY_TTPEVENTLISTENER_ONPOINTEREVENT)) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    private static void logTTPEventListenerOnPointerEvent(String key, Object... vars) {
        verityParamsType();
        Region touchExcludeRegion = (Region) vars[0];
        int x = ((Integer) vars[1]).intValue();
        int y = ((Integer) vars[2]).intValue();
        if (touchExcludeRegion == null) {
            Slog.e(TAG, key + "touchExcludeRegion == null");
            return;
        }
        Slog.i(TAG, key + ": " + touchExcludeRegion.toString() + ", x = " + x + ", y = " + y);
        if (!touchExcludeRegion.contains(x, y)) {
            Slog.i(TAG, "TouchExcludeRegion is not zoom window excludeRegion");
        }
    }

    private static void logTPControllerHandleTapOutSideTask(String key, Object... vars) {
        verityParamsType();
        Task task = (Task) vars[0];
        int x = ((Integer) vars[1]).intValue();
        int y = ((Integer) vars[2]).intValue();
        if (task == null) {
            Slog.i(TAG, key + ": task is null, x = " + x + ", y = " + y);
            return;
        }
        Slog.i(TAG, key + ": " + task.toString() + ", x = " + x + ", y = " + y);
    }

    private static void logZoomWindowServiceHandleTapOutSideTask(String key, Object... vars) {
        verityParamsType();
        int x = ((Integer) vars[0]).intValue();
        int y = ((Integer) vars[1]).intValue();
        Slog.i(TAG, key + ": x = " + x + ", y = " + y);
    }

    private static void verityParamsType() {
    }
}

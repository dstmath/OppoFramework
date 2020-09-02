package android.view;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.util.Log;

public class OppoScreenShotUtil {
    private static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String PERSIST_KEY = "sys.oppo.screenshot";
    private static final String TAG = "OppoScreenShotUtil";

    public static void pauseDeliverPointerEvent() {
        SystemProperties.set(PERSIST_KEY, WifiEnterpriseConfig.ENGINE_ENABLE);
    }

    public static void resumeDeliverPointerEvent() {
        SystemProperties.set(PERSIST_KEY, WifiEnterpriseConfig.ENGINE_DISABLE);
    }

    public static boolean checkPauseDeliverPointer() {
        return SystemProperties.getBoolean(PERSIST_KEY, false);
    }

    public static void dealPausedDeliverPointer(MotionEvent event, View view) {
        if (DEBUG) {
            Log.d(TAG, "dealPausedDeliverPointer ------------------------");
        }
        if (view != null) {
            event.setAction(3);
            view.dispatchPointerEvent(event);
        }
    }
}

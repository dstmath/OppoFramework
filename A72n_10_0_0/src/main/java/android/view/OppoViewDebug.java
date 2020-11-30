package android.view;

import android.util.Log;
import android.view.ViewTreeObserver;

public class OppoViewDebug {
    private static final String LOG_TAG = "View";
    private static boolean sAppCancelDraw = false;
    private static int sAppCancelDrawCount = 0;

    public static boolean firstCancelDrawListener() {
        return sAppCancelDraw;
    }

    public static void dispatchOnPreDraw(boolean cancelDraw, ViewTreeObserver.OnPreDrawListener listener) {
        if (!sAppCancelDraw && cancelDraw) {
            int i = sAppCancelDrawCount;
            if (i > 5 && i % 100 == 0) {
                Log.d(LOG_TAG, "dispatchOnPreDraw cancelDraw listener = " + listener + ",cancelDraw times =" + sAppCancelDrawCount);
            }
            sAppCancelDraw = cancelDraw;
        }
    }

    public static void performTraversals(boolean cancelDraw) {
        if (cancelDraw) {
            sAppCancelDrawCount++;
        } else {
            if (sAppCancelDrawCount > 5) {
                Log.d(LOG_TAG, "cancelDraw times  = " + sAppCancelDrawCount);
            }
            sAppCancelDrawCount = 0;
        }
        sAppCancelDraw = false;
    }
}

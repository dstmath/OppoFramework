package android.view.animation;

import android.content.Context;
import android.util.AttributeSet;

public class OppoAccelerateDecelerateInterpolator extends BaseInterpolator {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "OppoAccelerateDecelerateInterpolator";

    public OppoAccelerateDecelerateInterpolator(Context context, AttributeSet attrs) {
    }

    public float getInterpolation(float input) {
        if (input < 0.5f) {
            return (float) ((Math.cos((Math.sin((((double) input) * 3.141592653589793d) / 2.0d) + 1.0d) * 3.141592653589793d) + 1.0d) / 2.0d);
        }
        return (float) ((Math.cos((Math.sqrt((double) input) + 1.0d) * 3.141592653589793d) + 1.0d) / 2.0d);
    }
}

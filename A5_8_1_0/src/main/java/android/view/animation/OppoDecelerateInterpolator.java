package android.view.animation;

import android.content.Context;
import android.util.AttributeSet;

public class OppoDecelerateInterpolator extends BaseInterpolator {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "OppoDecelerateInterpolator";

    public OppoDecelerateInterpolator(Context context, AttributeSet attrs) {
    }

    public float getInterpolation(float input) {
        return (float) (1.0199999809265137d - (1.0199999809265137d / ((Math.pow((double) input, 2.0d) * 50.0d) + 1.0d)));
    }
}

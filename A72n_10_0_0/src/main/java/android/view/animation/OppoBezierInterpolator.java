package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

public class OppoBezierInterpolator extends BaseInterpolator {
    private final float ABOVE_ONE;
    private final float ABOVE_ZERO;
    private final float BELOW_ONE;
    private final boolean DEBUG;
    private final double EPSILON;
    private final String TAG;
    private boolean mAbove;
    private boolean mLimit;
    private OppoUnitBezier mOppoUnitBezier;

    public OppoBezierInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public OppoBezierInterpolator(Resources res, Resources.Theme theme, AttributeSet attrs) {
        this.TAG = "OppoBezierInterpolator";
        this.DEBUG = false;
        this.EPSILON = 6.25E-5d;
        this.ABOVE_ONE = 1.0f;
        this.BELOW_ONE = 0.9999f;
        this.ABOVE_ZERO = 1.0E-4f;
        this.mAbove = false;
        this.mLimit = false;
    }

    public OppoBezierInterpolator(double p1x, double p1y, double p2x, double p2y, boolean limit) {
        this.TAG = "OppoBezierInterpolator";
        this.DEBUG = false;
        this.EPSILON = 6.25E-5d;
        this.ABOVE_ONE = 1.0f;
        this.BELOW_ONE = 0.9999f;
        this.ABOVE_ZERO = 1.0E-4f;
        this.mAbove = false;
        this.mLimit = false;
        this.mLimit = limit;
        this.mOppoUnitBezier = new OppoUnitBezier(p1x, p1y, p2x, p2y);
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float input) {
        double interpolation = this.mOppoUnitBezier.solve((double) input, 6.25E-5d);
        if (this.mLimit) {
            if (input < 1.0E-4f || input > 0.9999f) {
                this.mAbove = false;
            }
            if (interpolation > 1.0d && !this.mAbove) {
                interpolation = 1.0d;
                this.mAbove = true;
            }
            if (this.mAbove) {
                interpolation = 1.0d;
            }
        }
        return (float) interpolation;
    }
}

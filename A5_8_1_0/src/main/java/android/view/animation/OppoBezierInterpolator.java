package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import oppo.R;

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

    public OppoBezierInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        this.TAG = "OppoBezierInterpolator";
        this.DEBUG = false;
        this.EPSILON = 6.25E-5d;
        this.ABOVE_ONE = 1.0f;
        this.BELOW_ONE = 0.9999f;
        this.ABOVE_ZERO = 1.0E-4f;
        this.mAbove = false;
        this.mLimit = false;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.oppoBezierInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.oppoBezierInterpolator);
        }
        float pointAx = a.getFloat(0, 0.5f);
        float pointAy = a.getFloat(1, 0.5f);
        float pointBx = a.getFloat(2, 0.7f);
        float pointBy = a.getFloat(3, 0.7f);
        this.mLimit = a.getBoolean(4, true);
        this.mOppoUnitBezier = new OppoUnitBezier((double) pointAx, (double) pointAy, (double) pointBx, (double) pointBy);
        a.recycle();
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

    public float getInterpolation(float input) {
        double interpolation = this.mOppoUnitBezier.solve((double) input, 6.25E-5d);
        if (this.mLimit) {
            if (input < 1.0E-4f || input > 0.9999f) {
                this.mAbove = false;
            }
            if (interpolation > 1.0d && (this.mAbove ^ 1) != 0) {
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

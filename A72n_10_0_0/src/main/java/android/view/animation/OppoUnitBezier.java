package android.view.animation;

public class OppoUnitBezier {
    private double ax;
    private double ay;
    private double bx;
    private double by;
    private double cx;
    private double cy;

    public OppoUnitBezier(double p1x, double p1y, double p2x, double p2y) {
        this.cx = p1x * 3.0d;
        double d = this.cx;
        this.bx = ((p2x - p1x) * 3.0d) - d;
        this.ax = (1.0d - d) - this.bx;
        this.cy = p1y * 3.0d;
        double d2 = this.cy;
        this.by = ((p2y - p1y) * 3.0d) - d2;
        this.ay = (1.0d - d2) - this.by;
    }

    public double sampleCurveX(double t) {
        return ((((this.ax * t) + this.bx) * t) + this.cx) * t;
    }

    public double sampleCurveY(double t) {
        return ((((this.ay * t) + this.by) * t) + this.cy) * t;
    }

    public double sampleCurveDerivativeX(double t) {
        return (((this.ax * 3.0d * t) + (this.bx * 2.0d)) * t) + this.cx;
    }

    public double solveCurveX(double x, double epsilon) {
        double t2 = x;
        for (int i = 0; i < 8; i++) {
            double x2 = sampleCurveX(t2) - x;
            if (Math.abs(x2) < epsilon) {
                return t2;
            }
            double d2 = sampleCurveDerivativeX(t2);
            if (Math.abs(d2) < 1.0E-6d) {
                break;
            }
            t2 -= x2 / d2;
        }
        double t0 = 0.0d;
        double t1 = 1.0d;
        double t22 = x;
        if (t22 < 0.0d) {
            return 0.0d;
        }
        if (t22 > 1.0d) {
            return 1.0d;
        }
        while (t0 < t1) {
            double x22 = sampleCurveX(t22);
            if (Math.abs(x22 - x) < epsilon) {
                return t22;
            }
            if (x > x22) {
                t0 = t22;
            } else {
                t1 = t22;
            }
            t22 = ((t1 - t0) * 0.5d) + t0;
        }
        return t22;
    }

    /* access modifiers changed from: package-private */
    public double solve(double x, double epsilon) {
        return sampleCurveY(solveCurveX(x, epsilon));
    }
}

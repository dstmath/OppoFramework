package android.view.animation;

public class OppoUnitBezier {
    private double ax = ((1.0d - this.cx) - this.bx);
    private double ay;
    private double bx;
    private double by;
    private double cx;
    private double cy;

    public OppoUnitBezier(double p1x, double p1y, double p2x, double p2y) {
        this.cx = 3.0d * p1x;
        this.bx = ((p2x - p1x) * 3.0d) - this.cx;
        this.cy = 3.0d * p1y;
        this.by = ((p2y - p1y) * 3.0d) - this.cy;
        this.ay = (1.0d - this.cy) - this.by;
    }

    public double sampleCurveX(double t) {
        return ((((this.ax * t) + this.bx) * t) + this.cx) * t;
    }

    public double sampleCurveY(double t) {
        return ((((this.ay * t) + this.by) * t) + this.cy) * t;
    }

    public double sampleCurveDerivativeX(double t) {
        return ((((this.ax * 3.0d) * t) + (this.bx * 2.0d)) * t) + this.cx;
    }

    public double solveCurveX(double x, double epsilon) {
        double x2;
        double t2 = x;
        for (int i = 0; i < 8; i++) {
            x2 = sampleCurveX(t2) - x;
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
        t2 = x;
        if (x < 0.0d) {
            return 0.0d;
        }
        if (x > 1.0d) {
            return 1.0d;
        }
        while (t0 < 1.0d) {
            x2 = sampleCurveX(t2);
            if (Math.abs(x2 - x) < epsilon) {
                return t2;
            }
            if (x > x2) {
            }
            t0 = t2;
            t2 = ((1.0d - t0) * 0.5d) + t0;
        }
        return t2;
    }

    double solve(double x, double epsilon) {
        return sampleCurveY(solveCurveX(x, epsilon));
    }
}

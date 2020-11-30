package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.common.ColorFrameworkFactory;
import android.content.Context;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class OverScroller {
    private static final int DEFAULT_DURATION = 250;
    private static final int FLING_MODE = 1;
    private static final int SCROLL_MODE = 0;
    private IColorOverScrollerHelper mColorOverScrollerHelper;
    private final boolean mFlywheel;
    @UnsupportedAppUsage
    private Interpolator mInterpolator;
    private int mMode;
    private final SplineOverScroller mScrollerX;
    @UnsupportedAppUsage
    private final SplineOverScroller mScrollerY;

    public OverScroller(Context context) {
        this(context, null);
    }

    public OverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    @UnsupportedAppUsage
    public OverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        this.mFlywheel = flywheel;
        this.mScrollerX = new SplineOverScroller(context);
        this.mScrollerY = new SplineOverScroller(context);
        this.mColorOverScrollerHelper = (IColorOverScrollerHelper) ColorFrameworkFactory.getInstance().getFeature(IColorOverScrollerHelper.DEFAULT, this);
    }

    @Deprecated
    public OverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    @Deprecated
    public OverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public final void setFriction(float friction) {
        if (!this.mColorOverScrollerHelper.setFriction(friction)) {
            this.mScrollerX.setFriction(friction);
            this.mScrollerY.setFriction(friction);
        }
    }

    public final boolean isFinished() {
        return this.mColorOverScrollerHelper.isFinished(this.mScrollerX.mFinished && this.mScrollerY.mFinished);
    }

    public final void forceFinished(boolean finished) {
        this.mScrollerX.mFinished = this.mScrollerY.mFinished = finished;
    }

    public final int getCurrX() {
        return this.mColorOverScrollerHelper.getCurrX(this.mScrollerX.mCurrentPosition);
    }

    public final int getCurrY() {
        return this.mColorOverScrollerHelper.getCurrY(this.mScrollerY.mCurrentPosition);
    }

    public float getCurrVelocity() {
        return (float) Math.hypot((double) this.mScrollerX.mCurrVelocity, (double) this.mScrollerY.mCurrVelocity);
    }

    public final int getStartX() {
        return this.mScrollerX.mStart;
    }

    public final int getStartY() {
        return this.mScrollerY.mStart;
    }

    public final int getFinalX() {
        return this.mColorOverScrollerHelper.getFinalX(this.mScrollerX.mFinal);
    }

    public final int getFinalY() {
        return this.mColorOverScrollerHelper.getFinalY(this.mScrollerY.mFinal);
    }

    @Deprecated
    public final int getDuration() {
        return Math.max(this.mScrollerX.mDuration, this.mScrollerY.mDuration);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void extendDuration(int extend) {
        this.mScrollerX.extendDuration(extend);
        this.mScrollerY.extendDuration(extend);
    }

    @Deprecated
    public void setFinalX(int newX) {
        this.mScrollerX.setFinalPosition(newX);
    }

    @Deprecated
    public void setFinalY(int newY) {
        this.mScrollerY.setFinalPosition(newY);
    }

    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }
        int i = this.mMode;
        if (i == 0) {
            long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mStartTime;
            int duration = this.mScrollerX.mDuration;
            if (elapsedTime < ((long) duration)) {
                float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                this.mScrollerX.updateScroll(q);
                this.mScrollerY.updateScroll(q);
            } else {
                abortAnimation();
            }
        } else if (i == 1) {
            if (!this.mScrollerX.mFinished && !this.mScrollerX.update() && !this.mScrollerX.continueWhenFinished()) {
                this.mScrollerX.finish();
            }
            if (!this.mScrollerY.mFinished && !this.mScrollerY.update() && !this.mScrollerY.continueWhenFinished()) {
                this.mScrollerY.finish();
            }
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mScrollerX.startScroll(startX, dx, duration);
        this.mScrollerY.startScroll(startY, dy, duration);
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        boolean spingbackX = this.mScrollerX.springback(startX, minX, maxX);
        boolean spingbackY = this.mScrollerY.springback(startY, minY, maxY);
        if (spingbackX || spingbackY) {
            return true;
        }
        return false;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        int velocityY2;
        int velocityX2 = velocityX;
        if (this.mFlywheel && !isFinished()) {
            float oldVelocityX = this.mScrollerX.mCurrVelocity;
            float oldVelocityY = this.mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX2) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX2 = (int) (((float) velocityX2) + oldVelocityX);
                velocityY2 = (int) (((float) velocityY) + oldVelocityY);
                this.mMode = 1;
                this.mScrollerX.fling(startX, velocityX2, minX, maxX, overX);
                this.mScrollerY.fling(startY, velocityY2, minY, maxY, overY);
            }
        }
        velocityY2 = velocityY;
        this.mMode = 1;
        this.mScrollerX.fling(startX, velocityX2, minX, maxX, overX);
        this.mScrollerY.fling(startY, velocityY2, minY, maxY, overY);
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public boolean isOverScrolled() {
        return (!this.mScrollerX.mFinished && this.mScrollerX.mState != 0) || (!this.mScrollerY.mFinished && this.mScrollerY.mState != 0);
    }

    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(this.mScrollerX.mStartTime, this.mScrollerY.mStartTime));
    }

    @UnsupportedAppUsage
    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !isFinished() && Math.signum(xvel) == Math.signum((float) (this.mScrollerX.mFinal - this.mScrollerX.mStart)) && Math.signum(yvel) == Math.signum((float) (this.mScrollerY.mFinal - this.mScrollerY.mStart));
    }

    /* access modifiers changed from: package-private */
    public static class SplineOverScroller {
        private static final int BALLISTIC = 2;
        private static final int CUBIC = 1;
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static final float END_TENSION = 1.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float INFLEXION = 0.35f;
        private static final int NB_SAMPLES = 100;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static final int SPLINE = 0;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private static final float START_TENSION = 0.5f;
        @UnsupportedAppUsage
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private int mOver;
        private float mPhysicalCoeff;
        private int mSplineDistance;
        private int mSplineDuration;
        private int mStart;
        private long mStartTime;
        private int mState = 0;
        private int mVelocity;

        static {
            float f;
            float x;
            float f2;
            float coef;
            float y;
            float coef2;
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < 100; i++) {
                float alpha = ((float) i) / 100.0f;
                float x_max = 1.0f;
                while (true) {
                    f = 2.0f;
                    x = ((x_max - x_min) / 2.0f) + x_min;
                    f2 = 3.0f;
                    coef = x * 3.0f * (1.0f - x);
                    float tx = ((((1.0f - x) * P1) + (x * P2)) * coef) + (x * x * x);
                    if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                        break;
                    } else if (tx > alpha) {
                        x_max = x;
                    } else {
                        x_min = x;
                    }
                }
                SPLINE_POSITION[i] = ((((1.0f - x) * 0.5f) + x) * coef) + (x * x * x);
                float y_max = 1.0f;
                while (true) {
                    y = ((y_max - y_min) / f) + y_min;
                    coef2 = y * f2 * (1.0f - y);
                    float dy = ((((1.0f - y) * 0.5f) + y) * coef2) + (y * y * y);
                    if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                        break;
                    } else if (dy > alpha) {
                        y_max = y;
                        f = 2.0f;
                        f2 = 3.0f;
                    } else {
                        y_min = y;
                        f = 2.0f;
                        f2 = 3.0f;
                    }
                }
                SPLINE_TIME[i] = (coef2 * (((1.0f - y) * P1) + (P2 * y))) + (y * y * y);
            }
            float[] fArr = SPLINE_POSITION;
            SPLINE_TIME[100] = 1.0f;
            fArr[100] = 1.0f;
        }

        /* access modifiers changed from: package-private */
        public void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        SplineOverScroller(Context context) {
            this.mPhysicalCoeff = 386.0878f * context.getResources().getDisplayMetrics().density * 160.0f * 0.84f;
        }

        /* access modifiers changed from: package-private */
        public void updateScroll(float q) {
            int i = this.mStart;
            this.mCurrentPosition = i + Math.round(((float) (this.mFinal - i)) * q);
        }

        private static float getDeceleration(int velocity) {
            if (velocity > 0) {
                return -2000.0f;
            }
            return GRAVITY;
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            float x = Math.abs(((float) (newFinal - start)) / ((float) (oldFinal - start)));
            int index = (int) (x * 100.0f);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float[] fArr = SPLINE_TIME;
                float t_inf = fArr[index];
                float t_sup = fArr[index + 1];
                this.mDuration = (int) (((float) this.mDuration) * ((((x - x_inf) / ((((float) (index + 1)) / 100.0f) - x_inf)) * (t_sup - t_inf)) + t_inf));
            }
        }

        /* access modifiers changed from: package-private */
        public void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        /* access modifiers changed from: package-private */
        public void finish() {
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
        }

        /* access modifiers changed from: package-private */
        public void setFinalPosition(int position) {
            this.mFinal = position;
            this.mFinished = false;
        }

        /* access modifiers changed from: package-private */
        public void extendDuration(int extend) {
            this.mDuration = ((int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) + extend;
            this.mFinished = false;
        }

        /* access modifiers changed from: package-private */
        public boolean springback(int start, int min, int max) {
            this.mFinished = true;
            this.mFinal = start;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mVelocity = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 0;
            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }
            return true ^ this.mFinished;
        }

        private void startSpringback(int start, int end, int velocity) {
            this.mFinished = false;
            this.mState = 1;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mFinal = end;
            int delta = start - end;
            this.mDeceleration = getDeceleration(delta);
            this.mVelocity = -delta;
            this.mOver = Math.abs(delta);
            this.mDuration = (int) (Math.sqrt((((double) delta) * -2.0d) / ((double) this.mDeceleration)) * 1000.0d);
        }

        /* access modifiers changed from: package-private */
        public void fling(int start, int velocity, int min, int max, int over) {
            this.mOver = over;
            this.mFinished = false;
            this.mVelocity = velocity;
            this.mCurrVelocity = (float) velocity;
            this.mSplineDuration = 0;
            this.mDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStart = start;
            this.mCurrentPosition = start;
            if (start > max || start < min) {
                startAfterEdge(start, min, max, velocity);
                return;
            }
            this.mState = 0;
            double totalDistance = 0.0d;
            if (velocity != 0) {
                int splineFlingDuration = getSplineFlingDuration(velocity);
                this.mSplineDuration = splineFlingDuration;
                this.mDuration = splineFlingDuration;
                totalDistance = getSplineFlingDistance(velocity);
            }
            this.mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            this.mFinal = this.mSplineDistance + start;
            int i = this.mFinal;
            if (i < min) {
                adjustDuration(this.mStart, i, min);
                this.mFinal = min;
            }
            int i2 = this.mFinal;
            if (i2 > max) {
                adjustDuration(this.mStart, i2, max);
                this.mFinal = max;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log((double) ((((float) Math.abs(velocity)) * INFLEXION) / (this.mFlingFriction * this.mPhysicalCoeff)));
        }

        private double getSplineFlingDistance(int velocity) {
            double l = getSplineDeceleration(velocity);
            float f = DECELERATION_RATE;
            return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) f) / (((double) f) - 1.0d)) * l);
        }

        private int getSplineFlingDuration(int velocity) {
            return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)) * 1000.0d);
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            float f = this.mDeceleration;
            float totalDuration = (float) Math.sqrt((((double) ((((((float) velocity) * ((float) velocity)) / 2.0f) / Math.abs(f)) + ((float) Math.abs(end - start)))) * 2.0d) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) ((totalDuration - (((float) (-velocity)) / f)) * 1000.0f));
            this.mStart = end;
            this.mCurrentPosition = end;
            this.mVelocity = (int) ((-this.mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            this.mDeceleration = getDeceleration(velocity == 0 ? start - end : velocity);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            boolean keepIncreasing = true;
            if (start <= min || start >= max) {
                boolean positive = start > max;
                int edge = positive ? max : min;
                int overDistance = start - edge;
                if (overDistance * velocity < 0) {
                    keepIncreasing = false;
                }
                if (keepIncreasing) {
                    startBounceAfterEdge(start, edge, velocity);
                } else if (getSplineFlingDistance(velocity) > ((double) Math.abs(overDistance))) {
                    fling(start, velocity, positive ? min : start, positive ? start : max, this.mOver);
                } else {
                    startSpringback(start, edge, velocity);
                }
            } else {
                Log.e("OverScroller", "startAfterEdge called from a valid position");
                this.mFinished = true;
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyEdgeReached(int start, int end, int over) {
            if (this.mState == 0) {
                this.mOver = over;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                startAfterEdge(start, end, end, (int) this.mCurrVelocity);
            }
        }

        private void onEdgeReached() {
            int i = this.mVelocity;
            float velocitySquared = ((float) i) * ((float) i);
            float distance = velocitySquared / (Math.abs(this.mDeceleration) * 2.0f);
            float sign = Math.signum((float) this.mVelocity);
            int i2 = this.mOver;
            if (distance > ((float) i2)) {
                this.mDeceleration = ((-sign) * velocitySquared) / (((float) i2) * 2.0f);
                distance = (float) i2;
            }
            this.mOver = (int) distance;
            this.mState = 2;
            this.mFinal = this.mStart + ((int) (this.mVelocity > 0 ? distance : -distance));
            this.mDuration = -((int) ((((float) this.mVelocity) * 1000.0f) / this.mDeceleration));
        }

        /* access modifiers changed from: package-private */
        public boolean continueWhenFinished() {
            int i = this.mState;
            if (i != 0) {
                if (i == 1) {
                    return false;
                }
                if (i == 2) {
                    this.mStartTime += (long) this.mDuration;
                    startSpringback(this.mFinal, this.mStart, 0);
                }
            } else if (this.mDuration >= this.mSplineDuration) {
                return false;
            } else {
                int i2 = this.mFinal;
                this.mStart = i2;
                this.mCurrentPosition = i2;
                this.mVelocity = (int) this.mCurrVelocity;
                this.mDeceleration = getDeceleration(this.mVelocity);
                this.mStartTime += (long) this.mDuration;
                onEdgeReached();
            }
            update();
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean update() {
            long currentTime = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            if (currentTime == 0) {
                return this.mDuration > 0;
            }
            int i = this.mDuration;
            if (currentTime > ((long) i)) {
                return false;
            }
            double distance = 0.0d;
            int i2 = this.mState;
            if (i2 == 0) {
                float t = ((float) currentTime) / ((float) this.mSplineDuration);
                int index = (int) (t * 100.0f);
                float distanceCoef = 1.0f;
                float velocityCoef = 0.0f;
                if (index < 100 && index >= 0) {
                    float t_inf = ((float) index) / 100.0f;
                    float[] fArr = SPLINE_POSITION;
                    float d_inf = fArr[index];
                    velocityCoef = (fArr[index + 1] - d_inf) / ((((float) (index + 1)) / 100.0f) - t_inf);
                    distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                }
                int i3 = this.mSplineDistance;
                distance = (double) (((float) i3) * distanceCoef);
                this.mCurrVelocity = ((((float) i3) * velocityCoef) / ((float) this.mSplineDuration)) * 1000.0f;
            } else if (i2 == 1) {
                float t2 = ((float) currentTime) / ((float) i);
                float t22 = t2 * t2;
                float sign = Math.signum((float) this.mVelocity);
                int i4 = this.mOver;
                distance = (double) (((float) i4) * sign * ((3.0f * t22) - ((2.0f * t2) * t22)));
                this.mCurrVelocity = ((float) i4) * sign * 6.0f * ((-t2) + t22);
            } else if (i2 == 2) {
                float t3 = ((float) currentTime) / 1000.0f;
                int i5 = this.mVelocity;
                float f = this.mDeceleration;
                this.mCurrVelocity = ((float) i5) + (f * t3);
                distance = (double) ((((float) i5) * t3) + (((f * t3) * t3) / 2.0f));
            }
            this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
            return true;
        }
    }
}

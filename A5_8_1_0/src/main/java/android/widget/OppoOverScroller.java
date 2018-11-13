package android.widget;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.color.util.ColorContextUtil;

public class OppoOverScroller extends OverScroller {
    private static final boolean DBG = false;
    private static final int DEFAULT_DURATION = 250;
    private static final float DEFAULT_TIME_GAP = 16.0f;
    private static final int FLING_MODE = 1;
    private static final int MAXIMUM_FLING_VELOCITY_LIST = 2500;
    private static final double PI = 3.1415926d;
    private static final int SCROLL_MODE = 0;
    private static int SPRING_BACK_DURATION = 250;
    private static String TAG = "OppoOverScroller";
    private static int mMaximumVelocity;
    private static int mOverscrollDistance;
    private final boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMode;
    private final OppoSplineOverScroller mScrollerX;
    private final OppoSplineOverScroller mScrollerY;

    static class OppoSplineOverScroller {
        private static final int BALLISTIC = 2;
        private static final int CUBIC = 1;
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static final float END_TENSION = 1.0f;
        private static final int FLING_SPLINE = 3;
        private static final float GRAVITY = 2000.0f;
        private static final float INFLEXION = 0.35f;
        private static final int NB_SAMPLES = 100;
        private static final int OVER_SPLINE = 4;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static final int SPLINE = 0;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private static final float START_TENSION = 0.5f;
        private static final float VISCOUS_FLUID_SCALE = 14.0f;
        private static float sViscousFluidNormalize;
        private AccelerateDecelerateInterpolator mAccelInterpolator;
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private boolean mIsScrollList = false;
        private double mLastDetla = 0.0d;
        private int mLastPosition;
        private int mOppoCount = 1;
        private int mOver;
        private boolean mOverSplineStart;
        private boolean mOverSpring = false;
        private float mPhysicalCoeff;
        private int mScrollerDistance;
        private int mSplineDistance;
        private int mSplineState;
        private int mStart;
        private long mStartTime;
        private float mStartV = 0.0f;
        private int mState = 0;
        private int mVelocity;

        void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        OppoSplineOverScroller(Context context) {
            this.mPhysicalCoeff = (386.0878f * (context.getResources().getDisplayMetrics().density * 160.0f)) * 0.84f;
            this.mAccelInterpolator = new AccelerateDecelerateInterpolator();
            sViscousFluidNormalize = 1.0f;
            sViscousFluidNormalize = 1.0f / viscousFluid(1.0f, VISCOUS_FLUID_SCALE);
        }

        void updateScroll(float q) {
            this.mCurrentPosition = this.mStart + Math.round(((float) (this.mFinal - this.mStart)) * q);
        }

        private static float getDeceleration(int velocity) {
            return velocity > 0 ? -2000.0f : GRAVITY;
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            float x = Math.abs(((float) (newFinal - start)) / ((float) (oldFinal - start)));
            int index = (int) (100.0f * x);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float x_sup = ((float) (index + 1)) / 100.0f;
                float t_inf = SPLINE_TIME[index];
                this.mDuration = (int) (((float) this.mDuration) * (t_inf + (((x - x_inf) / (x_sup - x_inf)) * (SPLINE_TIME[index + 1] - t_inf))));
            }
        }

        void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        void finish() {
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
        }

        void setFinalPosition(int position) {
            this.mFinal = position;
            this.mFinished = false;
        }

        void extendDuration(int extend) {
            this.mDuration = ((int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) + extend;
            this.mFinished = false;
        }

        boolean springback(int start, int min, int max) {
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
            return this.mFinished ^ 1;
        }

        private void startSpringback(int start, int end, int velocity) {
            this.mOppoCount = 1;
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

        void fling(int start, int velocity, int min, int max, int over) {
            this.mOppoCount = 1;
            this.mOver = over;
            this.mFinished = false;
            if (velocity > OppoOverScroller.mMaximumVelocity || velocity < (-OppoOverScroller.mMaximumVelocity)) {
                velocity = ((int) Math.signum((float) velocity)) * OppoOverScroller.mMaximumVelocity;
            }
            this.mVelocity = velocity;
            this.mCurrVelocity = (float) velocity;
            this.mDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mStartV = (float) velocity;
            this.mLastPosition = this.mStart;
            this.mOverSpring = false;
            if (start > max || start < min) {
                this.mOverSpring = true;
                startAfterEdge(start, min, max, velocity);
                return;
            }
            this.mState = 0;
            double totalDistance = 0.0d;
            if (velocity != 0) {
                this.mDuration = getSplineFlingDuration(velocity) + 100;
                totalDistance = getSplineFlingDistance(velocity);
            }
            this.mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            this.mFinal = this.mSplineDistance + start;
            if (this.mFinal < min) {
                this.mFinal = min;
            }
            if (this.mFinal > max) {
                this.mFinal = max;
            }
            if (!(over == 0 || (this.mIsScrollList ^ 1) == 0)) {
                this.mFinal = this.mStart;
                if (this.mFinal > OppoOverScroller.mOverscrollDistance || this.mFinal < (-OppoOverScroller.mOverscrollDistance)) {
                    this.mFinal = ((int) Math.signum((float) velocity)) * OppoOverScroller.mOverscrollDistance;
                }
                this.mStart = 0;
                this.mSplineState = 3;
                this.mState = 2;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log(((double) (((float) Math.abs(velocity)) * INFLEXION)) / (((double) this.mPhysicalCoeff) * 0.006d));
        }

        private double getSplineFlingDistance(int velocity) {
            return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - 1.0d)) * getSplineDeceleration(velocity));
        }

        private int getSplineFlingDuration(int velocity) {
            return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)) * 1000.0d);
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            float totalDuration = (float) Math.sqrt((((double) (((((float) (velocity * velocity)) / 2.0f) / Math.abs(this.mDeceleration)) + ((float) Math.abs(end - start)))) * 2.0d) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) ((totalDuration - (((float) (-velocity)) / this.mDeceleration)) * 1000.0f));
            this.mStart = end;
            this.mVelocity = (int) ((-this.mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            int i;
            this.mScrollerDistance = start;
            if (velocity == 0) {
                i = start - end;
            } else {
                i = velocity;
            }
            this.mDeceleration = getDeceleration(i);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            if (start <= min || start >= max) {
                boolean positive = start > max;
                int edge = positive ? max : min;
                int overDistance = start - edge;
                if (overDistance * velocity >= 0) {
                    startBounceAfterEdge(start, edge, velocity);
                } else if (getSplineFlingDistance(velocity) > ((double) Math.abs(overDistance))) {
                    fling(start, velocity, positive ? min : start, positive ? start : max, this.mOver);
                } else {
                    startSpringback(start, edge, velocity);
                }
                return;
            }
            Log.e("OverScroller", "startAfterEdge called from a valid position");
            this.mFinished = true;
        }

        void notifyEdgeReached(int start, int end, int over) {
            if (this.mState == 0) {
                this.mOver = over;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                this.mState = 1;
                startAfterEdge(start, end, end, (int) (this.mCurrVelocity / 1000.0f));
            }
        }

        private void onEdgeReached() {
            float sign = Math.signum((float) this.mVelocity);
            float distance = ((float) (this.mVelocity * this.mVelocity)) / 1600.0f;
            if (distance > ((float) this.mOver)) {
                this.mDeceleration = (((-sign) * ((float) this.mVelocity)) * ((float) this.mVelocity)) / (((float) this.mOver) * 2.0f);
                distance = (float) this.mOver;
            }
            this.mOppoCount = 1;
            this.mOver = (int) distance;
            this.mState = 2;
            int i = this.mStart;
            if (this.mVelocity <= 0) {
                distance = -distance;
            }
            this.mFinal = i + ((int) distance);
            this.mDuration = -((int) ((((float) this.mVelocity) * 1000.0f) / ((float) (this.mVelocity > 0 ? -800 : 800))));
            this.mSplineState = 4;
            this.mOverSplineStart = true;
        }

        boolean continueWhenFinished() {
            switch (this.mState) {
                case 0:
                    if (this.mIsScrollList && this.mOver != 0) {
                        int i = this.mFinal;
                        this.mStart = i;
                        this.mCurrentPosition = i;
                        this.mVelocity = ((int) this.mCurrVelocity) / 10;
                        this.mScrollerDistance = 0;
                        onEdgeReached();
                        break;
                    }
                    return false;
                case 1:
                    return false;
                case 2:
                    this.mStartTime += (long) this.mDuration;
                    startSpringback(this.mFinal, this.mStart, 0);
                    break;
            }
            update();
            return true;
        }

        boolean update() {
            long currentTime = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            int timePassed = (int) (((float) this.mOppoCount) * OppoOverScroller.DEFAULT_TIME_GAP);
            double distance = 0.0d;
            switch (this.mState) {
                case 0:
                    float currV = ((this.mStartV * ((float) ((Math.pow((double) ((Math.abs(this.mStartV) / 4500.0f) - 1.0f), 2.0d) * 0.3d) + 0.7d))) / 1500.0f) * ((float) (Math.cos((((((double) this.mOppoCount) * OppoOverScroller.PI) * 16.0d) / ((double) this.mDuration)) + 0.78539815d) + 1.0d));
                    distance = (double) (OppoOverScroller.DEFAULT_TIME_GAP * currV);
                    if ((Math.abs(distance) <= this.mLastDetla || this.mOppoCount <= 1) && this.mCurrVelocity != 0.0f) {
                        int delta = (int) Math.round(distance);
                        if (delta == 0) {
                            delta = (int) (Math.abs(distance) / distance);
                        }
                        this.mCurrentPosition = this.mLastPosition + delta;
                        this.mLastDetla = Math.abs(distance);
                        this.mLastPosition = this.mCurrentPosition;
                        this.mOppoCount++;
                        this.mCurrVelocity = 1000.0f * currV;
                        if (!this.mIsScrollList || ((delta <= 0 || this.mCurrentPosition < this.mFinal) && (delta >= 0 || this.mCurrentPosition > this.mFinal))) {
                            return true;
                        }
                        this.mCurrentPosition = this.mFinal;
                        return false;
                    }
                    this.mFinal = this.mCurrentPosition;
                    finish();
                    return false;
                case 1:
                    distance = (double) (((float) (this.mFinal - this.mStart)) * this.mAccelInterpolator.getInterpolation(Math.min(((float) timePassed) * (1.0f / ((float) OppoOverScroller.SPRING_BACK_DURATION)), 1.0f)));
                    this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
                    if (this.mCurrentPosition == this.mFinal) {
                        this.mCurrentPosition = this.mFinal;
                        finish();
                        return false;
                    }
                    break;
                case 2:
                    if (this.mSplineState != 4 || (this.mIsScrollList && (this.mOverSpring ^ 1) == 0)) {
                        this.mFinal = this.mCurrentPosition;
                        return false;
                    }
                    float x = viscousFluid(((float) timePassed) * (1.0f / ((float) this.mDuration)), VISCOUS_FLUID_SCALE);
                    distance = (double) (((float) (this.mFinal - this.mStart)) * x);
                    while (true) {
                        if (((this.mFinal < 0 && distance >= ((double) this.mScrollerDistance)) || (this.mFinal > 0 && distance <= ((double) this.mScrollerDistance))) && (this.mIsScrollList ^ 1) != 0) {
                            this.mOppoCount++;
                            x = viscousFluid(((float) ((int) (((float) this.mOppoCount) * OppoOverScroller.DEFAULT_TIME_GAP))) * (1.0f / ((float) this.mDuration)), VISCOUS_FLUID_SCALE);
                            distance = (double) (((float) (this.mFinal - this.mStart)) * x);
                            this.mOverSplineStart = true;
                        }
                    }
                    if (this.mOverSplineStart) {
                        x = viscousFluid(((float) ((int) (((float) (this.mOppoCount + 1)) * OppoOverScroller.DEFAULT_TIME_GAP))) * (1.0f / ((float) this.mDuration)), VISCOUS_FLUID_SCALE);
                        double nextDistance = (double) (((float) (this.mFinal - this.mStart)) * x);
                        if ((this.mFinal < 0 && nextDistance - distance < distance - ((double) this.mScrollerDistance)) || (this.mFinal > 0 && nextDistance - distance > distance - ((double) this.mScrollerDistance))) {
                            distance = nextDistance;
                            this.mOppoCount++;
                        }
                        this.mOverSplineStart = false;
                    }
                    if ((this.mFinal < 0 && this.mCurrentPosition <= this.mFinal) || ((this.mFinal > 0 && this.mCurrentPosition >= this.mFinal) || ((double) x) > 0.91d || Math.round(distance) == 0)) {
                        this.mFinal = this.mStart + ((int) Math.round(distance));
                        return false;
                    }
                    break;
            }
            this.mOppoCount++;
            this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
            return true;
        }

        private static float viscousFluid(float x, float distance) {
            return (0.36787945f + ((1.0f - ((float) Math.exp((double) (1.0f - ((x + ((1.0f - ((float) Math.log((double) (1.0f / 0.63212055f)))) / distance)) * distance))))) * 0.63212055f)) * sViscousFluidNormalize;
        }
    }

    public OppoOverScroller(Context context) {
        this(context, null);
    }

    public OppoOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public OppoOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        this.mFlywheel = flywheel;
        this.mScrollerX = new OppoSplineOverScroller(context);
        this.mScrollerY = new OppoSplineOverScroller(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mOverscrollDistance = metrics.heightPixels;
        mMaximumVelocity = (int) (metrics.density * 2500.0f);
        if (mMaximumVelocity > 9000) {
            mMaximumVelocity = 9000;
        }
    }

    public OppoOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    public OppoOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    static OverScroller newInstance(Context context) {
        if (ColorContextUtil.isColorStyle(context)) {
            return new OppoOverScroller(context);
        }
        return new OverScroller(context);
    }

    static OverScroller newInstance(Context context, boolean isScrollList) {
        if (!ColorContextUtil.isColorStyle(context)) {
            return new OverScroller(context);
        }
        OverScroller mScroller = new OppoOverScroller(context);
        OppoOverScroller mOppoScroller = (OppoOverScroller) mScroller;
        mOppoScroller.mScrollerX.mIsScrollList = mOppoScroller.mScrollerY.mIsScrollList = isScrollList;
        return mScroller;
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public void setOppoFriction(float friction) {
        this.mScrollerX.setFriction(friction);
        this.mScrollerY.setFriction(friction);
    }

    public boolean isOppoFinished() {
        return this.mScrollerX.mFinished ? this.mScrollerY.mFinished : false;
    }

    public final void oppoForceFinished(boolean finished) {
        this.mScrollerX.mFinished = this.mScrollerY.mFinished = finished;
    }

    public int getOppoCurrX() {
        return this.mScrollerX.mCurrentPosition;
    }

    public int getOppoCurrY() {
        return this.mScrollerY.mCurrentPosition;
    }

    public float getCurrVelocity() {
        return FloatMath.sqrt((this.mScrollerX.mCurrVelocity * this.mScrollerX.mCurrVelocity) + (this.mScrollerY.mCurrVelocity * this.mScrollerY.mCurrVelocity));
    }

    public final int getOppoStartX() {
        return this.mScrollerX.mStart;
    }

    public final int getOppoStartY() {
        return this.mScrollerY.mStart;
    }

    public int getOppoFinalX() {
        return this.mScrollerX.mFinal;
    }

    public int getOppoFinalY() {
        return this.mScrollerY.mFinal;
    }

    @Deprecated
    public int getOppoDuration() {
        return Math.max(this.mScrollerX.mDuration, this.mScrollerY.mDuration);
    }

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
        switch (this.mMode) {
            case 0:
                long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mStartTime;
                int duration = this.mScrollerX.mDuration;
                if (elapsedTime >= ((long) duration)) {
                    abortAnimation();
                    break;
                }
                float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                this.mScrollerX.updateScroll(q);
                this.mScrollerY.updateScroll(q);
                break;
            case 1:
                if (!(this.mScrollerX.mFinished || this.mScrollerX.update() || this.mScrollerX.continueWhenFinished())) {
                    this.mScrollerX.finish();
                }
                if (!(this.mScrollerY.mFinished || this.mScrollerY.update() || this.mScrollerY.continueWhenFinished())) {
                    this.mScrollerY.finish();
                    break;
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
        return !this.mScrollerX.springback(startX, minX, maxX) ? this.mScrollerY.springback(startY, minY, maxY) : true;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        if (this.mFlywheel && (isFinished() ^ 1) != 0) {
            float oldVelocityX = this.mScrollerX.mCurrVelocity;
            float oldVelocityY = this.mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        this.mMode = 1;
        this.mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        this.mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public boolean isOverScrolled() {
        if (!this.mScrollerX.mFinished && this.mScrollerX.mState != 0) {
            return true;
        }
        if (this.mScrollerY.mFinished) {
            return false;
        }
        if (this.mScrollerY.mState == 0) {
            return false;
        }
        return true;
    }

    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(this.mScrollerX.mStartTime, this.mScrollerY.mStartTime));
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        int dx = this.mScrollerX.mFinal - this.mScrollerX.mStart;
        int dy = this.mScrollerY.mFinal - this.mScrollerY.mStart;
        if (!isFinished() && Math.signum(xvel) == Math.signum((float) dx) && Math.signum(yvel) == Math.signum((float) dy)) {
            return true;
        }
        return false;
    }
}

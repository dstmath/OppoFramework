package android.widget;

import android.content.Context;
import android.os.SystemProperties;
import android.util.FloatMath;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Scroller;
import com.color.util.ColorContextUtil;

public class OppoOverScroller extends OverScroller {
    private static final boolean DBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int DEFAULT_DURATION = 250;
    private static final float DEFAULT_TIME_GAP = 16.0f;
    private static final int FLING_MODE = 1;
    private static final int MAXIMUM_FLING_VELOCITY_LIST = 2500;
    private static final int MAX_VELOCITY = 9000;
    public static final float OPPO_FLING_FRICTION_FAST = 1.65f;
    public static final float OPPO_FLING_FRICTION_NORMAL = 2.05f;
    public static final int OPPO_FLING_MODE_FAST = 0;
    public static final int OPPO_FLING_MODE_NORMAL = 1;
    private static final double PI = 3.1415926d;
    private static final int SCROLL_MODE = 0;
    private static final int SPRING_BACK_DURATION = 600;
    private static final String TAG = "OppoOverScroller";
    private static int sMaximumVelocity;
    private static int sOverscrollDistance;
    private final boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMode;
    private final OppoSplineOverScroller mScrollerX;
    private final OppoSplineOverScroller mScrollerY;

    public OppoOverScroller(Context context) {
        this(context, null);
    }

    public OppoOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public OppoOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        this.mFlywheel = flywheel;
        this.mScrollerX = new OppoSplineOverScroller(context);
        this.mScrollerY = new OppoSplineOverScroller(context);
        sOverscrollDistance = context.getResources().getDisplayMetrics().heightPixels;
    }

    public OppoOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    public OppoOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    public void setEnableScrollList(boolean flag) {
        this.mScrollerX.mIsScrollList = flag;
        this.mScrollerY.mIsScrollList = flag;
    }

    public static OverScroller newInstance(Context context) {
        if (ColorContextUtil.isColorStyle(context)) {
            return new OppoOverScroller(context);
        }
        return new OverScroller(context);
    }

    public static OverScroller newInstance(Context context, boolean isScrollList) {
        if (!ColorContextUtil.isColorStyle(context)) {
            return new OverScroller(context);
        }
        OverScroller mScroller = new OppoOverScroller(context);
        OppoOverScroller mOppoScroller = (OppoOverScroller) mScroller;
        mOppoScroller.mScrollerX.mIsScrollList = isScrollList;
        mOppoScroller.mScrollerY.mIsScrollList = isScrollList;
        return mScroller;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.OverScroller
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public void setOppoFriction(float friction) {
        this.mScrollerX.setFriction(friction);
        this.mScrollerY.setFriction(friction);
    }

    public boolean isOppoFinished() {
        return this.mScrollerX.mFinished && this.mScrollerY.mFinished;
    }

    public final void oppoForceFinished(boolean finished) {
        this.mScrollerX.mFinished = finished;
        this.mScrollerY.mFinished = finished;
    }

    public int getOppoCurrX() {
        return this.mScrollerX.mCurrentPosition;
    }

    public int getOppoCurrY() {
        return this.mScrollerY.mCurrentPosition;
    }

    @Override // android.widget.OverScroller
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

    @Override // android.widget.OverScroller
    @Deprecated
    public void extendDuration(int extend) {
        this.mScrollerX.extendDuration(extend);
        this.mScrollerY.extendDuration(extend);
    }

    @Override // android.widget.OverScroller
    @Deprecated
    public void setFinalX(int newX) {
        this.mScrollerX.setFinalPosition(newX);
    }

    @Override // android.widget.OverScroller
    @Deprecated
    public void setFinalY(int newY) {
        this.mScrollerY.setFinalPosition(newY);
    }

    @Override // android.widget.OverScroller
    public boolean computeScrollOffset() {
        if (isOppoFinished()) {
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

    @Override // android.widget.OverScroller
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    @Override // android.widget.OverScroller
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mScrollerX.startScroll(startX, dx, duration);
        this.mScrollerY.startScroll(startY, dy, duration);
    }

    @Override // android.widget.OverScroller
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        boolean spingbackX = this.mScrollerX.springback(startX, minX, maxX);
        boolean spingbackY = this.mScrollerY.springback(startY, minY, maxY);
        if (spingbackX || spingbackY) {
            return true;
        }
        return false;
    }

    @Override // android.widget.OverScroller
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    @Override // android.widget.OverScroller
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        int velocityY2;
        int velocityX2 = velocityX;
        if (this.mFlywheel && !isOppoFinished()) {
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

    @Override // android.widget.OverScroller
    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    @Override // android.widget.OverScroller
    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    @Override // android.widget.OverScroller
    public boolean isOverScrolled() {
        return (!this.mScrollerX.mFinished && this.mScrollerX.mState != 0) || (!this.mScrollerY.mFinished && this.mScrollerY.mState != 0);
    }

    @Override // android.widget.OverScroller
    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }

    @Override // android.widget.OverScroller
    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(this.mScrollerX.mStartTime, this.mScrollerY.mStartTime));
    }

    @Override // android.widget.OverScroller
    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !isOppoFinished() && Math.signum(xvel) == Math.signum((float) (this.mScrollerX.mFinal - this.mScrollerX.mStart)) && Math.signum(yvel) == Math.signum((float) (this.mScrollerY.mFinal - this.mScrollerY.mStart));
    }

    public void setFlingFriction(float friction) {
        this.mScrollerX.mOrigamiFriction = friction;
        this.mScrollerY.mOrigamiFriction = friction;
    }

    public void setOppoFlingMode(int mode) {
        if (mode == 0) {
            setFlingFriction(1.65f);
        } else if (mode != 1) {
            throw new IllegalArgumentException("wrong fling argument");
        }
    }

    /* access modifiers changed from: package-private */
    public static class OppoSplineOverScroller {
        private static final int BALLISTIC = 2;
        private static final float BALLISTIC_THRESHOLD = 0.91f;
        private static final float BASE_DENSITY_FACTOR = 160.0f;
        private static final int CUBIC = 1;
        private static final float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static final float END_TENSION = 1.0f;
        private static final float FLING_CHANGE_INCREASE_STEP = 1.2f;
        private static final float FLING_CHANGE_REDUCE_STEP = 0.6f;
        private static final float FLING_CONTROL_ONE_X = 0.0f;
        private static final float FLING_CONTROL_ONE_Y = 0.17f;
        private static final float FLING_CONTROL_TWO_X = 0.25f;
        private static final float FLING_CONTROL_TWO_Y = 0.85f;
        private static final float FLING_DXDT_RATIO = 0.0694f;
        private static final int FLING_SPLINE = 3;
        private static final float FLOAT_1 = 1.0f;
        private static final float FLOAT_2 = 2.0f;
        private static final float FLOAT_25 = 25.0f;
        private static final float FLOAT_3 = 3.0f;
        private static final float FLOAT_8 = 8.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float INCH_METER = 39.37f;
        private static final float INFLEXION = 0.35f;
        private static final int NB_SAMPLES = 100;
        private static final int NUM_10 = 10;
        private static final int NUM_100 = 100;
        private static final int NUM_1000 = 1000;
        private static final int NUM_60 = 60;
        private static final int NUM_800 = 800;
        private static final int OVER_SPLINE = 4;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static final float PHYSICAL_COFF_FACTOR = 0.84f;
        private static final double SOLVER_TIMESTEP_SEC = 0.016d;
        private static final int SPLINE = 0;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private static final float START_TENSION = 0.5f;
        private static final float VISCOUS_FLUID_SCALE = 14.0f;
        private static float sTimeIncrease = 1.0f;
        private static float sViscousFluidNormalize;
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private double mEndValue;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private PathInterpolator mFlingInterpolator;
        private boolean mIsScrollList = false;
        private double mLastDetla = 0.0d;
        private int mLastPosition;
        private double mLastVelocity;
        private int mOppoCount = 1;
        private float mOrigamiFriction = 2.05f;
        private int mOver;
        private boolean mOverSplineStart;
        private boolean mOverSpring = false;
        private float mPhysicalCoeff;
        private float mReboundFriction;
        private float mReboundTension = 0.0f;
        private int mRestThreshold = 40;
        private int mScrollerDistance;
        private int mSplineDistance;
        private int mSplineState;
        private int mStart;
        private long mStartTime;
        private float mStartV = 0.0f;
        private int mState = 0;
        private int mVelocity;

        /* access modifiers changed from: package-private */
        public void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        OppoSplineOverScroller(Context context) {
            this.mPhysicalCoeff = 386.0878f * context.getResources().getDisplayMetrics().density * BASE_DENSITY_FACTOR * PHYSICAL_COFF_FACTOR;
            sViscousFluidNormalize = 1.0f;
            sViscousFluidNormalize = 1.0f / viscousFluid(1.0f, VISCOUS_FLUID_SCALE);
            this.mFlingInterpolator = new PathInterpolator(0.0f, FLING_CONTROL_ONE_Y, 0.25f, FLING_CONTROL_TWO_Y);
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
            this.mCurrentPosition = start;
            this.mStart = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        /* access modifiers changed from: package-private */
        public void finish() {
            this.mCurrentPosition = this.mFinal;
            sTimeIncrease = 1.0f;
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
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "springback start=" + start + ", min=" + min + ", max=" + max);
            }
            this.mFinished = true;
            this.mCurrentPosition = start;
            this.mStart = start;
            this.mFinal = start;
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
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "startSpringback start=" + start + ", end=" + end + ", velocity=" + velocity);
            }
            this.mOppoCount = 1;
            this.mFinished = false;
            this.mState = 1;
            this.mCurrentPosition = start;
            this.mStart = start;
            this.mFinal = end;
            int delta = start - end;
            this.mDeceleration = getDeceleration(delta);
            this.mVelocity = -delta;
            this.mOver = Math.abs(delta);
            this.mDuration = (int) (Math.sqrt((((double) delta) * -2.0d) / ((double) this.mDeceleration)) * 1000.0d);
        }

        /* access modifiers changed from: package-private */
        public void fling(int start, int velocity, int min, int max, int over) {
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "fling start=" + start + ", velocity=" + velocity + ", min=" + min + ", max=" + max + ", over=" + over);
            }
            this.mOppoCount = 1;
            this.mOver = over;
            this.mFinished = false;
            this.mReboundFriction = frictionFromOrigamiValue(this.mOrigamiFriction);
            this.mCurrVelocity = (float) velocity;
            this.mVelocity = velocity;
            this.mDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mCurrentPosition = start;
            this.mStart = start;
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
                this.mEndValue = totalDistance;
                this.mLastVelocity = (double) velocity;
            }
            this.mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            this.mFinal = this.mSplineDistance + start;
            if (this.mFinal < min) {
                this.mFinal = min;
            }
            if (this.mFinal > max) {
                this.mFinal = max;
            }
            if (over != 0 && !this.mIsScrollList) {
                this.mFinal = this.mStart;
                if (this.mFinal > OppoOverScroller.sOverscrollDistance || this.mFinal < (-OppoOverScroller.sOverscrollDistance)) {
                    this.mFinal = ((int) Math.signum((float) velocity)) * OppoOverScroller.sOverscrollDistance;
                }
                this.mStart = 0;
                this.mSplineState = 3;
                this.mState = 2;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log((double) ((((float) Math.abs(velocity)) * INFLEXION) / (this.mPhysicalCoeff * 0.006f)));
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
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "fitOnBounceCurve() start=" + start + ", end=" + end + ", velocity=" + velocity);
            }
            float f = this.mDeceleration;
            float totalDuration = (float) Math.sqrt((((double) (((((float) (velocity * velocity)) / FLOAT_2) / Math.abs(f)) + ((float) Math.abs(end - start)))) * 2.0d) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) ((totalDuration - (((float) (-velocity)) / f)) * 1000.0f));
            this.mStart = end;
            this.mVelocity = (int) ((-this.mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "startBounceAfterEdge() start=" + start + ", end=" + end + ", velocity=" + velocity);
            }
            this.mScrollerDistance = start;
            this.mDeceleration = getDeceleration(velocity == 0 ? start - end : velocity);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            boolean keepIncreasing = true;
            if (start <= min || start >= max) {
                if (OppoOverScroller.DBG) {
                    Log.d(OppoOverScroller.TAG, "startAfterEdge() start=" + start + ", min=" + min + ", max=" + max + ", velocity=" + velocity);
                }
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
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "notifyEdgeReached() start=" + start + ", end=" + end + ", over=" + over);
            }
            if (this.mState == 0) {
                this.mOver = over;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                this.mState = 1;
                startAfterEdge(start, end, end, (int) (this.mCurrVelocity / 1000.0f));
            }
        }

        private void onEdgeReached() {
            float sign = Math.signum((float) this.mVelocity);
            int i = this.mVelocity;
            float distance = ((float) (i * i)) / 1600.0f;
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "onEdgeReached() mVelocity=" + this.mVelocity + ", distance=" + distance + ", mOver=" + this.mOver + ", mDeceleration=" + this.mDeceleration);
            }
            int i2 = this.mOver;
            if (distance > ((float) i2)) {
                int i3 = this.mVelocity;
                this.mDeceleration = (((-sign) * ((float) i3)) * ((float) i3)) / (((float) i2) * FLOAT_2);
                distance = (float) i2;
            }
            this.mOppoCount = 1;
            this.mOver = (int) distance;
            this.mState = 2;
            this.mFinal = this.mStart + ((int) (this.mVelocity > 0 ? distance : -distance));
            this.mDuration = -((this.mVelocity * 1000) / (this.mVelocity > 0 ? -800 : 800));
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "onEdgeReached() mFinal=" + this.mFinal + ", mDuration=" + this.mDuration);
            }
            this.mSplineState = 4;
            this.mOverSplineStart = true;
        }

        /* access modifiers changed from: package-private */
        public boolean continueWhenFinished() {
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "continueWhenFinished mState=" + this.mState);
            }
            int i = this.mState;
            if (i != 0) {
                if (i == 1) {
                    return false;
                }
                if (i == 2) {
                    this.mStartTime += (long) this.mDuration;
                    startSpringback(this.mFinal, this.mStart, 0);
                }
            } else if (!this.mIsScrollList || this.mOver == 0) {
                return false;
            } else {
                int i2 = this.mFinal;
                this.mCurrentPosition = i2;
                this.mStart = i2;
                this.mVelocity = ((int) this.mCurrVelocity) / 10;
                this.mScrollerDistance = 0;
                onEdgeReached();
            }
            update();
            return true;
        }

        /* JADX INFO: Multiple debug info for r1v4 double: [D('position' double), D('velocity' double)] */
        /* access modifiers changed from: package-private */
        public boolean update() {
            boolean z;
            float x;
            int i;
            long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            int timePassed = (int) (((float) this.mOppoCount) * OppoOverScroller.DEFAULT_TIME_GAP);
            if (OppoOverScroller.DBG) {
                Log.d(OppoOverScroller.TAG, "update() mState=" + this.mState + ", mOppoCount=" + this.mOppoCount + ", mCurrentPosition=" + this.mCurrentPosition);
            }
            double distance = 0.0d;
            int i2 = this.mState;
            if (i2 != 0) {
                if (i2 == 1) {
                    float x2 = this.mFlingInterpolator.getInterpolation(Math.min(((float) timePassed) * 0.0016666667f, 1.0f));
                    int i3 = this.mFinal;
                    int i4 = this.mStart;
                    distance = (double) (((float) (i3 - i4)) * x2);
                    this.mCurrentPosition = i4 + ((int) Math.round(distance));
                    if (OppoOverScroller.DBG) {
                        Log.d(OppoOverScroller.TAG, "update CUBIC x=" + x2 + ", distance=" + distance + ", mFinal=" + this.mFinal + ", mCurrentPosition=" + this.mCurrentPosition);
                    }
                    int i5 = this.mCurrentPosition;
                    int i6 = this.mFinal;
                    if (i5 == i6) {
                        this.mCurrentPosition = i6;
                        finish();
                        return false;
                    }
                } else if (i2 == 2) {
                    if (this.mSplineState != 4) {
                        z = false;
                    } else if (!this.mIsScrollList || !this.mOverSpring) {
                        float x3 = viscousFluid(((float) timePassed) * (1.0f / ((float) this.mDuration)), VISCOUS_FLUID_SCALE);
                        distance = (double) (((float) (this.mFinal - this.mStart)) * x3);
                        if (OppoOverScroller.DBG) {
                            Log.d(OppoOverScroller.TAG, "update mSplineState == OVER_SPLINE x=" + x3 + ", distance=" + distance + ", mFinal=" + this.mFinal + ", mScrollerDistance=" + this.mScrollerDistance);
                        }
                        while (true) {
                            if (((this.mFinal < 0 && distance >= ((double) this.mScrollerDistance)) || (this.mFinal > 0 && distance <= ((double) this.mScrollerDistance))) && !this.mIsScrollList) {
                                this.mOppoCount++;
                                x3 = viscousFluid(((float) ((int) (((float) this.mOppoCount) * OppoOverScroller.DEFAULT_TIME_GAP))) * (1.0f / ((float) this.mDuration)), VISCOUS_FLUID_SCALE);
                                distance = (double) (((float) (this.mFinal - this.mStart)) * x3);
                                if (OppoOverScroller.DBG) {
                                    Log.d(OppoOverScroller.TAG, "update while mOppoCount=" + this.mOppoCount + ", distance=" + distance);
                                }
                                this.mOverSplineStart = true;
                            }
                        }
                        if (this.mOverSplineStart) {
                            float x4 = viscousFluid(((float) ((int) (((float) (this.mOppoCount + 1)) * OppoOverScroller.DEFAULT_TIME_GAP))) * (1.0f / ((float) this.mDuration)), VISCOUS_FLUID_SCALE);
                            int i7 = this.mFinal;
                            double nextDistance = (double) (((float) (i7 - this.mStart)) * x4);
                            if ((i7 < 0 && nextDistance - distance < distance - ((double) this.mScrollerDistance)) || (this.mFinal > 0 && nextDistance - distance > distance - ((double) this.mScrollerDistance))) {
                                distance = nextDistance;
                                this.mOppoCount++;
                            }
                            this.mOverSplineStart = false;
                            x = x4;
                        } else {
                            x = x3;
                        }
                        int i8 = this.mFinal;
                        if ((i8 < 0 && this.mCurrentPosition <= i8) || (((i = this.mFinal) > 0 && this.mCurrentPosition >= i) || x > BALLISTIC_THRESHOLD || Math.round(distance) == 0)) {
                            this.mFinal = this.mStart + ((int) Math.round(distance));
                            return false;
                        }
                    } else {
                        z = false;
                    }
                    this.mFinal = this.mCurrentPosition;
                    return z;
                }
                this.mOppoCount++;
                this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
                return true;
            }
            double position = (double) this.mLastPosition;
            double velocity = this.mLastVelocity;
            if (this.mOppoCount < 60) {
                sTimeIncrease += 0.020000001f;
                this.mReboundFriction += 0.020000001f;
            } else {
                float f = sTimeIncrease;
                sTimeIncrease = f - ((f - 0.6f) / 60.0f);
                this.mReboundFriction -= (sTimeIncrease - 0.6f) / 60.0f;
            }
            float f2 = this.mReboundTension;
            double tempVelocity = this.mEndValue;
            float f3 = this.mReboundFriction;
            double aAcceleration = (((double) f2) * (tempVelocity - 0.0d)) - (((double) f3) * this.mLastVelocity);
            double tempVelocity2 = velocity + ((aAcceleration * SOLVER_TIMESTEP_SEC) / 2.0d);
            double bAcceleration = (((double) f2) * (tempVelocity - (((velocity * SOLVER_TIMESTEP_SEC) / 2.0d) + position))) - (((double) f3) * tempVelocity2);
            double tempVelocity3 = velocity + ((bAcceleration * SOLVER_TIMESTEP_SEC) / 2.0d);
            double cAcceleration = (((double) f2) * (tempVelocity - (position + ((tempVelocity2 * SOLVER_TIMESTEP_SEC) / 2.0d)))) - (((double) f3) * tempVelocity3);
            double tempVelocity4 = velocity + (cAcceleration * SOLVER_TIMESTEP_SEC);
            double dAcceleration = (((double) f2) * (tempVelocity - (position + (tempVelocity3 * SOLVER_TIMESTEP_SEC)))) - (((double) f3) * tempVelocity4);
            double position2 = position + ((((tempVelocity2 + tempVelocity3) * 2.0d) + velocity + tempVelocity4) * 0.06939999759197235d * SOLVER_TIMESTEP_SEC);
            float currV = (float) (velocity + ((aAcceleration + ((bAcceleration + cAcceleration) * 2.0d) + dAcceleration) * 0.06939999759197235d * SOLVER_TIMESTEP_SEC));
            double distance2 = ((double) currV) * SOLVER_TIMESTEP_SEC;
            if (Math.abs(distance2) <= this.mLastDetla || this.mOppoCount <= 1) {
                float f4 = this.mCurrVelocity;
                int i9 = this.mRestThreshold;
                if (f4 <= ((float) (-i9)) || f4 >= ((float) i9)) {
                    int delta = (int) Math.round(distance2);
                    if (delta == 0) {
                        delta = (int) (Math.abs(distance2) / distance2);
                    }
                    this.mCurrentPosition = this.mLastPosition + delta;
                    this.mLastDetla = Math.abs(distance2);
                    int i10 = this.mCurrentPosition;
                    this.mLastPosition = i10;
                    this.mOppoCount++;
                    this.mCurrVelocity = currV;
                    this.mLastVelocity = (double) currV;
                    if (!this.mIsScrollList) {
                        return true;
                    }
                    if ((delta <= 0 || i10 < this.mFinal) && (delta >= 0 || this.mCurrentPosition > this.mFinal)) {
                        return true;
                    }
                    this.mCurrentPosition = this.mFinal;
                    return false;
                }
            }
            this.mFinal = this.mCurrentPosition;
            finish();
            return false;
        }

        private static float viscousFluid(float x, float distance) {
            return (((1.0f - 0.36787945f) * (1.0f - ((float) Math.exp((double) (1.0f - ((((1.0f - ((float) Math.log((double) (1.0f / (1.0f - 0.36787945f))))) / distance) + x) * distance)))))) + 0.36787945f) * sViscousFluidNormalize;
        }

        private float frictionFromOrigamiValue(float value) {
            if (value == 0.0f) {
                return 0.0f;
            }
            return ((value - FLOAT_8) * FLOAT_3) + FLOAT_25;
        }
    }
}

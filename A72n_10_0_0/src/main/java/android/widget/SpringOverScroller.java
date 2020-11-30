package android.widget;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.color.util.ColorContextUtil;

public class SpringOverScroller extends OverScroller {
    private static final int FLING_MODE = 1;
    public static final float OPPO_FLING_FRICTION_FAST = 0.76f;
    public static final float OPPO_FLING_FRICTION_NORMAL = 1.06f;
    public static final int OPPO_FLING_MODE_FAST = 0;
    public static final int OPPO_FLING_MODE_NORMAL = 1;
    private static final int REST_MODE = 2;
    private static final int SCROLL_DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final float SOLVER_TIMESTEP_SEC = 0.016f;
    private Interpolator mInterpolator;
    private int mMode;
    private ReboundOverScroller mScrollerX;
    private ReboundOverScroller mScrollerY;

    public static OverScroller newInstance(Context context) {
        if (ColorContextUtil.isColorStyle(context)) {
            return new SpringOverScroller(context);
        }
        return new OverScroller(context);
    }

    public static OverScroller newInstance(Context context, boolean isScrollView) {
        if (!ColorContextUtil.isColorStyle(context)) {
            return new OverScroller(context);
        }
        OverScroller scroller = new SpringOverScroller(context);
        SpringOverScroller s = (SpringOverScroller) scroller;
        s.mScrollerX.mIsScrollView = isScrollView;
        s.mScrollerY.mIsScrollView = isScrollView;
        return scroller;
    }

    public SpringOverScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
        float refreshTime;
        this.mMode = 2;
        Display display = ((DisplayManager) context.getSystemService(DisplayManager.class)).getDisplay(0);
        if (display == null) {
            refreshTime = SOLVER_TIMESTEP_SEC;
        } else {
            refreshTime = ((float) Math.round(10000.0f / display.getRefreshRate())) / 10000.0f;
        }
        this.mScrollerX = new ReboundOverScroller(refreshTime);
        this.mScrollerY = new ReboundOverScroller(refreshTime);
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public SpringOverScroller(Context context) {
        this(context, null);
    }

    public void setFlingFriction(float friction) {
        this.mScrollerX.mFlingFriction = friction;
        this.mScrollerY.mFlingFriction = friction;
    }

    @Override // android.widget.OverScroller
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    @Override // android.widget.OverScroller
    public boolean computeScrollOffset() {
        if (isOppoFinished()) {
            return false;
        }
        int i = this.mMode;
        if (i == 0) {
            long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mScrollStartTime;
            int duration = this.mScrollerX.mScrollDuration;
            if (elapsedTime < ((long) duration)) {
                float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                this.mScrollerX.updateScroll(q);
                this.mScrollerY.updateScroll(q);
            } else {
                this.mScrollerX.updateScroll(1.0f);
                this.mScrollerY.updateScroll(1.0f);
                abortAnimation();
            }
        } else if (i == 1 && !this.mScrollerX.update() && !this.mScrollerY.update()) {
            abortAnimation();
        }
        return true;
    }

    public final boolean isOppoFinished() {
        return this.mScrollerX.isAtRest() && this.mScrollerY.isAtRest() && this.mMode != 0;
    }

    public final int getOppoCurrX() {
        return (int) Math.round(this.mScrollerX.getCurrentValue());
    }

    public final int getOppoCurrY() {
        return (int) Math.round(this.mScrollerY.getCurrentValue());
    }

    public final int getOppoFinalX() {
        return (int) this.mScrollerX.getEndValue();
    }

    public final int getOppoFinalY() {
        return (int) this.mScrollerY.getEndValue();
    }

    @Override // android.widget.OverScroller
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        if (startY <= maxY) {
            if (startY >= minY) {
                fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                return;
            }
        }
        springBack(startX, startY, minX, maxX, minY, maxY);
    }

    @Override // android.widget.OverScroller
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY) {
        this.mMode = 1;
        this.mScrollerX.fling(startX, velocityX);
        this.mScrollerY.fling(startY, velocityY);
    }

    @Override // android.widget.OverScroller
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        boolean springBackX = this.mScrollerX.springBack(startX, minX, maxX);
        boolean springBackY = this.mScrollerY.springBack(startY, minY, maxY);
        if (springBackX || springBackY) {
            this.mMode = 1;
        }
        if (springBackX || springBackY) {
            return true;
        }
        return false;
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
    public void abortAnimation() {
        this.mMode = 2;
        this.mScrollerX.setAtRest();
        this.mScrollerY.setAtRest();
    }

    @Override // android.widget.OverScroller
    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !isFinished() && Math.signum(xvel) == Math.signum((float) ((int) (this.mScrollerX.mEndValue - this.mScrollerX.mStartValue))) && Math.signum(yvel) == Math.signum((float) ((int) (this.mScrollerY.mEndValue - this.mScrollerY.mStartValue)));
    }

    @Override // android.widget.OverScroller
    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
        springBack(0, startY, 0, 0, 0, 0);
    }

    @Override // android.widget.OverScroller
    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
        springBack(startX, 0, 0, 0, 0, 0);
    }

    @Override // android.widget.OverScroller
    public float getCurrVelocity() {
        double velX = this.mScrollerX.getVelocity();
        double velY = this.mScrollerY.getVelocity();
        return (float) ((int) Math.sqrt((velX * velX) + (velY * velY)));
    }

    public void setOppoFriction(float friction) {
    }

    /* access modifiers changed from: package-private */
    public static class ReboundOverScroller {
        private static final float FLING_CHANGE_INCREASE_STEP = 1.2f;
        private static final float FLING_CHANGE_REDUCE_STEP = 0.6f;
        private static final float FLING_DXDT_RATIO = 0.167f;
        private static final float FLOAT_1 = 1.0f;
        private static final float FLOAT_2 = 2.0f;
        private static final int NUM_60 = 60;
        private static final int SPRING_BACK_ADJUST_TENSION_VALUE = 100;
        private static final int SPRING_BACK_ADJUST_THRESHOLD = 180;
        private static final float SPRING_BACK_FRICTION = 12.19f;
        private static final int SPRING_BACK_STOP_THRESHOLD = 2;
        private static final float SPRING_BACK_TENSION = 16.0f;
        private static float sTimeIncrease = 1.0f;
        private ReboundConfig mConfig;
        private PhysicsState mCurrentState = new PhysicsState();
        private double mDisplacementFromRestThreshold = 0.05d;
        private double mEndValue;
        private ReboundConfig mFlingConfig;
        private float mFlingFriction = 1.06f;
        private boolean mIsScrollView = false;
        private boolean mIsSpringBack;
        private int mOppoCount = 1;
        private PhysicsState mPreviousState = new PhysicsState();
        private float mRefreshTime;
        private double mRestSpeedThreshold = 100.0d;
        private int mScrollDuration;
        private int mScrollFinal;
        private int mScrollStart;
        private long mScrollStartTime;
        private ReboundConfig mSpringBackConfig;
        private double mStartValue;
        private PhysicsState mTempState = new PhysicsState();
        private boolean mTensionAdjusted;

        ReboundOverScroller(float refreshTime) {
            this.mRefreshTime = refreshTime;
            this.mFlingConfig = new ReboundConfig((double) this.mFlingFriction, 0.0d);
            this.mSpringBackConfig = new ReboundConfig(12.1899995803833d, 16.0d);
            setConfig(this.mFlingConfig);
        }

        /* access modifiers changed from: package-private */
        public void fling(int start, int velocity) {
            this.mOppoCount = 1;
            sTimeIncrease = 1.0f;
            this.mFlingConfig.setFriction((double) this.mFlingFriction);
            this.mFlingConfig.setTension(0.0d);
            setConfig(this.mFlingConfig);
            setCurrentValue((double) start, true);
            setVelocity((double) velocity);
        }

        /* access modifiers changed from: package-private */
        public boolean springBack(int start, int min, int max) {
            setCurrentValue((double) start, false);
            if (start > max || start < min) {
                if (start > max) {
                    setEndValue((double) max);
                } else if (start < min) {
                    setEndValue((double) min);
                }
                this.mIsSpringBack = true;
                this.mSpringBackConfig.setFriction(12.1899995803833d);
                this.mSpringBackConfig.setTension(16.0d);
                setConfig(this.mSpringBackConfig);
                return true;
            }
            setConfig(new ReboundConfig((double) this.mFlingFriction, 0.0d));
            return false;
        }

        /* access modifiers changed from: package-private */
        public void startScroll(int start, int distance, int duration) {
            this.mScrollStart = start;
            this.mScrollFinal = start + distance;
            this.mScrollDuration = duration;
            this.mScrollStartTime = AnimationUtils.currentAnimationTimeMillis();
            setConfig(this.mFlingConfig);
        }

        /* access modifiers changed from: package-private */
        public void setConfig(ReboundConfig config) {
            if (config != null) {
                this.mConfig = config;
                return;
            }
            throw new IllegalArgumentException("springConfig is required");
        }

        /* access modifiers changed from: package-private */
        public void setCurrentValue(double currentValue, boolean setAtRest) {
            this.mStartValue = currentValue;
            if (!this.mIsScrollView) {
                this.mPreviousState.mPosition = 0.0d;
                this.mTempState.mPosition = 0.0d;
            }
            this.mCurrentState.mPosition = currentValue;
            if (setAtRest) {
                setAtRest();
            }
        }

        /* access modifiers changed from: package-private */
        public double getCurrentValue() {
            return this.mCurrentState.mPosition;
        }

        /* access modifiers changed from: package-private */
        public double getVelocity() {
            return this.mCurrentState.mVelocity;
        }

        /* access modifiers changed from: package-private */
        public void setVelocity(double velocity) {
            if (velocity != this.mCurrentState.mVelocity) {
                this.mCurrentState.mVelocity = velocity;
            }
        }

        /* access modifiers changed from: package-private */
        public double getEndValue() {
            return this.mEndValue;
        }

        /* access modifiers changed from: package-private */
        public void setEndValue(double endValue) {
            if (this.mEndValue != endValue) {
                this.mStartValue = getCurrentValue();
                this.mEndValue = endValue;
            }
        }

        /* access modifiers changed from: package-private */
        public void setAtRest() {
            this.mEndValue = this.mCurrentState.mPosition;
            this.mTempState.mPosition = this.mCurrentState.mPosition;
            this.mCurrentState.mVelocity = 0.0d;
            this.mIsSpringBack = false;
        }

        /* access modifiers changed from: package-private */
        public boolean isAtRest() {
            return Math.abs(this.mCurrentState.mVelocity) <= this.mRestSpeedThreshold && (getDisplacementDistanceForState(this.mCurrentState) <= this.mDisplacementFromRestThreshold || this.mConfig.mTension == 0.0d);
        }

        /* access modifiers changed from: package-private */
        public void updateScroll(float q) {
            PhysicsState physicsState = this.mCurrentState;
            int i = this.mScrollStart;
            physicsState.mPosition = (double) (i + Math.round(((float) (this.mScrollFinal - i)) * q));
        }

        /* access modifiers changed from: package-private */
        public void notifyEdgeReached(int start, int end, int over) {
            this.mCurrentState.mPosition = (double) start;
            PhysicsState physicsState = this.mPreviousState;
            physicsState.mPosition = 0.0d;
            physicsState.mVelocity = 0.0d;
            PhysicsState physicsState2 = this.mTempState;
            physicsState2.mPosition = 0.0d;
            physicsState2.mVelocity = 0.0d;
        }

        /* access modifiers changed from: package-private */
        public double getDisplacementDistanceForState(PhysicsState state) {
            return Math.abs(this.mEndValue - state.mPosition);
        }

        /* access modifiers changed from: package-private */
        public boolean update() {
            if (isAtRest()) {
                return false;
            }
            double position = this.mCurrentState.mPosition;
            double velocity = this.mCurrentState.mVelocity;
            double tempPosition = this.mTempState.mPosition;
            double d = this.mTempState.mVelocity;
            if (this.mIsSpringBack) {
                double displacement = getDisplacementDistanceForState(this.mCurrentState);
                if (!this.mTensionAdjusted && displacement < 180.0d) {
                    this.mConfig.mTension += 100.0d;
                    this.mTensionAdjusted = true;
                } else if (displacement < 2.0d) {
                    this.mCurrentState.mPosition = this.mEndValue;
                    this.mTensionAdjusted = false;
                    this.mIsSpringBack = false;
                    return false;
                }
            } else if (this.mOppoCount < 60) {
                sTimeIncrease += 0.020000001f;
                this.mConfig.mFriction += 0.020000001415610313d;
            } else {
                float f = sTimeIncrease;
                sTimeIncrease = f - ((f - 0.6f) / 60.0f);
                this.mConfig.mFriction -= (double) ((sTimeIncrease - 0.6f) / 60.0f);
            }
            double aAcceleration = (this.mConfig.mTension * (this.mEndValue - tempPosition)) - (this.mConfig.mFriction * this.mPreviousState.mVelocity);
            float f2 = this.mRefreshTime;
            double tempVelocity = ((((double) f2) * aAcceleration) / 2.0d) + velocity;
            double bAcceleration = (this.mConfig.mTension * (this.mEndValue - (((((double) f2) * velocity) / 2.0d) + position))) - (this.mConfig.mFriction * tempVelocity);
            float f3 = this.mRefreshTime;
            double tempVelocity2 = ((((double) f3) * bAcceleration) / 2.0d) + velocity;
            double cAcceleration = (this.mConfig.mTension * (this.mEndValue - (((((double) f3) * tempVelocity) / 2.0d) + position))) - (this.mConfig.mFriction * tempVelocity2);
            float f4 = this.mRefreshTime;
            double tempPosition2 = (((double) f4) * tempVelocity2) + position;
            double tempVelocity3 = (((double) f4) * cAcceleration) + velocity;
            double dAcceleration = (this.mConfig.mTension * (this.mEndValue - tempPosition2)) - (this.mConfig.mFriction * tempVelocity3);
            float f5 = this.mRefreshTime;
            double position2 = position + (((double) f5) * (velocity + ((tempVelocity + tempVelocity2) * 2.0d) + tempVelocity3) * 0.16699999570846558d);
            double dxdt = (double) f5;
            PhysicsState physicsState = this.mTempState;
            physicsState.mVelocity = tempVelocity3;
            physicsState.mPosition = tempPosition2;
            PhysicsState physicsState2 = this.mCurrentState;
            physicsState2.mVelocity = velocity + (dxdt * (aAcceleration + ((bAcceleration + cAcceleration) * 2.0d) + dAcceleration) * 0.16699999570846558d);
            physicsState2.mPosition = position2;
            this.mOppoCount++;
            return true;
        }

        /* access modifiers changed from: package-private */
        public static class ReboundConfig {
            double mFriction;
            double mTension;

            ReboundConfig(double friction, double tension) {
                this.mFriction = (double) frictionFromOrigamiValue((float) friction);
                this.mTension = tensionFromOrigamiValue((float) tension);
            }

            /* access modifiers changed from: package-private */
            public void setFriction(double friction) {
                this.mFriction = (double) frictionFromOrigamiValue((float) friction);
            }

            /* access modifiers changed from: package-private */
            public void setTension(double tension) {
                this.mTension = tensionFromOrigamiValue((float) tension);
            }

            private float frictionFromOrigamiValue(float value) {
                if (value == 0.0f) {
                    return 0.0f;
                }
                return ((value - 8.0f) * 3.0f) + 25.0f;
            }

            private double tensionFromOrigamiValue(float oValue) {
                if (oValue == 0.0f) {
                    return 0.0d;
                }
                return (double) (((oValue - 30.0f) * 3.62f) + 194.0f);
            }
        }

        /* access modifiers changed from: package-private */
        public static class PhysicsState {
            double mPosition;
            double mVelocity;

            PhysicsState() {
            }
        }
    }
}

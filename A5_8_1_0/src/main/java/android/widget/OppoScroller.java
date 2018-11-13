package android.widget;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class OppoScroller {
    private static final int DEFAULT_DURATION = 250;
    private static final int DEFAULT_TIME_GAP = 15;
    private static final int FLING_MODE = 1;
    private static final int FLING_SCROLL_BACK_DURATION = 750;
    private static final int FLING_SCROLL_BACK_MODE = 3;
    private static final int FLING_SPRING_MODE = 2;
    private static final int GALLERY_LIST_MODE = 5;
    private static final int GALLERY_TIME_GAP = 25;
    private static final int SCROLL_LIST_MODE = 4;
    private static final int SCROLL_MODE = 0;
    final boolean DEBUG_SPRING;
    private int DeltaCurrV;
    final String TAG;
    private int fmCurrY;
    private int fmLastCurrY;
    private float mCoeffX;
    private float mCoeffY;
    private int mCount;
    private int mCurrV;
    private int mCurrVX;
    private int mCurrVY;
    private int mCurrX;
    private int mCurrY;
    public float mDeceleration;
    private float mDeltaX;
    private float mDeltaY;
    private int mDuration;
    private float mDurationReciprocal;
    private int mFinalX;
    private int mFinalY;
    private boolean mFinished;
    private Interpolator mInterpolator;
    private int mLastCurrV;
    private int mLastCurrY;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private int mMode;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;
    private float mViscousFluidNormalize;
    private float mViscousFluidScale;

    public OppoScroller(Context context) {
        this(context, null);
    }

    public OppoScroller(Context context, Interpolator interpolator) {
        this.TAG = "OppoScroller";
        this.DEBUG_SPRING = false;
        this.mLastCurrV = 0;
        this.mCurrV = 0;
        this.DeltaCurrV = 0;
        this.mCoeffX = 0.0f;
        this.mCoeffY = 1.0f;
        this.mCount = 1;
        this.fmLastCurrY = 0;
        this.mLastCurrY = 0;
        this.mFinished = true;
        this.mInterpolator = interpolator;
        this.mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        this.mDeceleration = (this.mPpi * 386.0878f) * ViewConfiguration.getScrollFriction();
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public final void forceFinished(boolean finished) {
        this.mFinished = finished;
    }

    public final int getDuration() {
        return this.mDuration;
    }

    public final int getCurrX() {
        return this.mCurrX;
    }

    public final int getCurrY() {
        return this.mCurrY;
    }

    public float getCurrVelocity() {
        return this.mVelocity - ((this.mDeceleration * ((float) timePassed())) / 2000.0f);
    }

    public final int getStartX() {
        return this.mStartX;
    }

    public final int getStartY() {
        return this.mStartY;
    }

    public final int getFinalX() {
        return this.mFinalX;
    }

    public final int getFinalY() {
        return this.mFinalY;
    }

    public final int getCurrVX() {
        return this.mCurrVX;
    }

    public final int getCurrVY() {
        return this.mCurrVY;
    }

    public boolean computeScrollOffset() {
        if (this.mFinished) {
            return false;
        }
        int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
        if (4 == this.mMode) {
            timePassed = this.mCount * 15;
        } else if (5 == this.mMode) {
            timePassed = this.mCount * 25;
        }
        if (timePassed < this.mDuration) {
            float x;
            float timePassedSeconds;
            float distance;
            switch (this.mMode) {
                case 0:
                case 5:
                    x = ((float) timePassed) * this.mDurationReciprocal;
                    if (this.mInterpolator == null) {
                        x = viscousFluid(x);
                    } else {
                        x = this.mInterpolator.getInterpolation(x);
                    }
                    this.mCurrX = this.mStartX + Math.round(this.mDeltaX * x);
                    this.mCurrY = this.mStartY + Math.round(this.mDeltaY * x);
                    if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                        this.mFinished = true;
                        break;
                    }
                case 1:
                    timePassedSeconds = ((float) timePassed) / 1000.0f;
                    distance = (this.mVelocity * timePassedSeconds) - (((this.mDeceleration * timePassedSeconds) * timePassedSeconds) / 2.0f);
                    this.mCurrX = this.mStartX + Math.round(this.mCoeffX * distance);
                    this.mCurrX = Math.min(this.mCurrX, this.mMaxX);
                    this.mCurrX = Math.max(this.mCurrX, this.mMinX);
                    this.mCurrY = this.mStartY + Math.round(this.mCoeffY * distance);
                    this.mCurrY = Math.min(this.mCurrY, this.mMaxY);
                    this.mCurrY = Math.max(this.mCurrY, this.mMinY);
                    float velocity = this.mVelocity - (this.mDeceleration * timePassedSeconds);
                    this.mCurrVX = Math.round(this.mCoeffX * velocity);
                    this.mCurrVY = Math.round(this.mCoeffY * velocity);
                    if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                        this.mFinished = true;
                        break;
                    }
                case 2:
                    timePassedSeconds = ((float) timePassed) / 1000.0f;
                    distance = (this.mVelocity * timePassedSeconds) - (((this.mDeceleration * timePassedSeconds) * timePassedSeconds) / 2.0f);
                    this.mCurrX = this.mStartX + Math.round(this.mCoeffX * distance);
                    this.mCurrX = Math.min(this.mCurrX, this.mMaxX);
                    this.mCurrX = Math.max(this.mCurrX, this.mMinX);
                    this.mCurrY = this.mStartY + Math.round(this.mCoeffY * distance);
                    this.mCurrY = Math.min(this.mCurrY, this.mMaxY);
                    this.mCurrY = Math.max(this.mCurrY, this.mMinY);
                    if (this.mCurrX == this.mFinalX || this.mCurrY == this.mFinalY) {
                        startScroll(this.mCurrX, this.mCurrY, (int) (this.mDeltaX + ((float) (this.mFinalX - this.mCurrX))), (int) (this.mDeltaY + ((float) (this.mFinalY - this.mCurrY))), 750);
                        this.mMode = 3;
                        break;
                    }
                case 3:
                    x = viscousFluid(((float) timePassed) * this.mDurationReciprocal);
                    this.mCurrX = this.mStartX + Math.round(this.mDeltaX * x);
                    this.mCurrY = this.mStartY + Math.round(this.mDeltaY * x);
                    if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                        this.mFinished = true;
                        break;
                    }
                case 4:
                    x = ((float) (timePassed + 200)) / 2000.0f;
                    if (timePassed == 15) {
                        this.DeltaCurrV = 0;
                        this.mLastCurrV = 0;
                        this.fmLastCurrY = 0;
                    }
                    x = ((1.0f - ((float) Math.exp((double) (-4.0f * x)))) - 1.0f) + ((float) Math.exp(-0.4000000059604645d));
                    this.mCurrX = this.mStartX + ((int) (this.mDeltaX * x));
                    this.fmCurrY = (this.mStartY + ((int) (this.mDeltaY * x))) - this.DeltaCurrV;
                    this.mCurrV = this.fmCurrY - this.fmLastCurrY;
                    if (this.mLastCurrV != 0 && Math.abs(this.mLastCurrV) < Math.abs(this.mCurrV)) {
                        this.DeltaCurrV += this.mCurrV - this.mLastCurrV;
                        this.fmCurrY -= this.mCurrV - this.mLastCurrV;
                        this.mCurrV = this.mLastCurrV;
                    }
                    this.fmLastCurrY = this.fmCurrY;
                    this.mLastCurrV = this.mCurrV;
                    this.mCurrY = this.fmCurrY;
                    this.mCurrVY = Math.round((float) (((this.mCurrY - this.mLastCurrY) * 250) / 15));
                    this.mLastCurrY = this.mCurrY;
                    if ((this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) || this.mCurrVY == 0) {
                        this.mFinalY = this.mCurrY;
                        this.mFinished = true;
                        break;
                    }
            }
        } else if (this.mMode == 2) {
            startScroll(this.mCurrX, this.mCurrY, (int) (this.mDeltaX + ((float) (this.mFinalX - this.mCurrX))), (int) (this.mDeltaY + ((float) (this.mFinalY - this.mCurrY))), 750);
            this.mMode = 3;
            return true;
        } else {
            this.mCurrX = this.mFinalX;
            this.mCurrY = this.mFinalY;
            this.mFinished = true;
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    public void startScrollList(int startX, int startY, int dx, int dy, int duration) {
        startScroll(startX, startY, dx, dy, duration);
        this.mMode = 4;
        this.mViscousFluidNormalize = 1.0f;
        this.mViscousFluidNormalize = 1.0f / getInterpolation(1.0f);
        this.mLastCurrY = 0;
        this.mCount = 1;
    }

    public void startGalleryList(int startX, int startY, int dx, int dy, int duration) {
        startScroll(startX, startY, dx, dy, duration);
        this.mMode = 5;
        this.mCount = 1;
    }

    public void setCount(int n) {
        this.mCount = n;
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mFinished = false;
        this.mDuration = duration;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        this.mFinalX = startX + dx;
        this.mFinalY = startY + dy;
        this.mDeltaX = (float) dx;
        this.mDeltaY = (float) dy;
        this.mDurationReciprocal = 1.0f / ((float) this.mDuration);
        this.mViscousFluidScale = 8.0f;
        this.mViscousFluidNormalize = 1.0f;
        this.mViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        this.mFinished = false;
        float velocity = (float) Math.hypot((double) velocityX, (double) velocityY);
        this.mVelocity = velocity;
        this.mDuration = (int) ((1000.0f * velocity) / this.mDeceleration);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        this.mCoeffX = velocity == 0.0f ? 1.0f : ((float) velocityX) / velocity;
        this.mCoeffY = velocity == 0.0f ? 1.0f : ((float) velocityY) / velocity;
        int totalDistance = (int) ((velocity * velocity) / (this.mDeceleration * 2.0f));
        this.mMinX = minX;
        this.mMaxX = maxX;
        this.mMinY = minY;
        this.mMaxY = maxY;
        this.mFinalX = Math.round(((float) totalDistance) * this.mCoeffX) + startX;
        this.mFinalX = Math.min(this.mFinalX, this.mMaxX);
        this.mFinalX = Math.max(this.mFinalX, this.mMinX);
        this.mFinalY = Math.round(((float) totalDistance) * this.mCoeffY) + startY;
        this.mFinalY = Math.min(this.mFinalY, this.mMaxY);
        this.mFinalY = Math.max(this.mFinalY, this.mMinY);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int springOffsetX, int springOffsetY) {
        fling(startX, startY, velocityX, velocityY, minX - springOffsetX, maxX + springOffsetX, minY - springOffsetY, maxY + springOffsetY);
        this.mDeltaY = 0.0f;
        this.mDeltaX = 0.0f;
        if (this.mFinalX > maxX || this.mFinalX < minX) {
            this.mMode = 2;
            if (this.mFinalX > maxX) {
                this.mDeltaX = (float) (maxX - this.mFinalX);
            } else {
                this.mDeltaX = (float) (minX - this.mFinalX);
            }
        }
        if (this.mFinalY > maxY || this.mFinalY < minY) {
            this.mMode = 2;
            if (this.mFinalY > maxY) {
                this.mDeltaY = (float) (maxY - this.mFinalY);
            } else {
                this.mDeltaY = (float) (minY - this.mFinalY);
            }
        }
    }

    private float viscousFluid(float x) {
        x *= this.mViscousFluidScale;
        if (x < 1.0f) {
            x -= 1.0f - ((float) Math.exp((double) (-x)));
        } else {
            x = 0.36787945f + (0.63212055f * (1.0f - ((float) Math.exp((double) (1.0f - x)))));
        }
        return x * this.mViscousFluidNormalize;
    }

    private float getInterpolation(float x) {
        return ((1.0f - ((float) Math.exp((double) (-(x * 12.0f))))) * 0.63212055f) * this.mViscousFluidNormalize;
    }

    public void abortAnimation() {
        this.mCurrX = this.mFinalX;
        this.mCurrY = this.mFinalY;
        this.mFinished = true;
    }

    public void extendDuration(int extend) {
        this.mDuration = timePassed() + extend;
        this.mDurationReciprocal = 1.0f / ((float) this.mDuration);
        this.mFinished = false;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
    }

    public void setFinalX(int newX) {
        this.mFinalX = newX;
        this.mDeltaX = (float) (this.mFinalX - this.mStartX);
        this.mFinished = false;
    }

    public void setFinalY(int newY) {
        this.mFinalY = newY;
        this.mDeltaY = (float) (this.mFinalY - this.mStartY);
        this.mFinished = false;
    }

    public final void setFriction(float friction) {
        this.mDeceleration = computeDeceleration(friction);
    }

    private float computeDeceleration(float friction) {
        return (this.mPpi * 386.0878f) * friction;
    }
}

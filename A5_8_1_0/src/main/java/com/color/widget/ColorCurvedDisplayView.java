package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import java.util.HashMap;

public class ColorCurvedDisplayView extends View {
    public static final String BLUE = "blue";
    private static final int FADE_OUT_ALPHA_END = 0;
    private static final int FADE_OUT_ALPHA_START = 255;
    private static final int INCALL_ANIMATION_INTERNEL_DELAY = 2500;
    private static final float INCALL_FADEOUT_BEZIER_CONTROL_ONE_X = 0.42f;
    private static final float INCALL_FADEOUT_BEZIER_CONTROL_ONE_Y = 0.0f;
    private static final float INCALL_FADEOUT_BEZIER_CONTROL_TWO_X = 0.52f;
    private static final float INCALL_FADEOUT_BEZIER_CONTROL_TWO_Y = 1.0f;
    private static final int INCALL_FADE_OUT_DELAY = 2500;
    private static final int INCALL_FADE_OUT_DURATION = 2500;
    private static final float INCALL_MASK_BEZIER_CONTROL_ONE_X = 0.37f;
    private static final float INCALL_MASK_BEZIER_CONTROL_ONE_Y = 0.57f;
    private static final float INCALL_MASK_BEZIER_CONTROL_TWO_X = 0.35f;
    private static final float INCALL_MASK_BEZIER_CONTROL_TWO_Y = 0.62f;
    private static final int INCALL_MASK_MOVE_DURATION = 5000;
    private static final int INCALL_MASK_MOVE_END = 5250;
    private static final int INCALL_MASK_MOVE_START = 0;
    private static final float NOTIFICATION_FADEOUT_BEZIER_CONTROL_ONE_X = 0.5f;
    private static final float NOTIFICATION_FADEOUT_BEZIER_CONTROL_ONE_Y = 0.0f;
    private static final float NOTIFICATION_FADEOUT_BEZIER_CONTROL_TWO_X = 0.85f;
    private static final float NOTIFICATION_FADEOUT_BEZIER_CONTROL_TWO_Y = 1.0f;
    private static final int NOTIFICATION_FADE_OUT_DELAY = 1800;
    private static final int NOTIFICATION_FADE_OUT_DURATION = 2200;
    private static final float NOTIFICATION_MASK_BEZIER_CONTROL_ONE_X = 0.37f;
    private static final float NOTIFICATION_MASK_BEZIER_CONTROL_ONE_Y = 0.37f;
    private static final float NOTIFICATION_MASK_BEZIER_CONTROL_TWO_X = 0.16f;
    private static final float NOTIFICATION_MASK_BEZIER_CONTROL_TWO_Y = 0.77f;
    private static final int NOTIFICATION_MASK_MOVE_DURATION = 4000;
    private static final int NOTIFICATION_MASK_MOVE_END = 3100;
    private static final int NOTIFICATION_MASK_MOVE_START = 200;
    public static final String ORANGE = "orange";
    public static final String RED = "red";
    private int mAnimRepeatCount = 0;
    private String mColor;
    private int[] mEdgeIds;
    private int mIncallOffsetY1;
    private int mIncallOffsetY2;
    private int mIncallPositionY1;
    private int mIncallPositionY2;
    private boolean mIsIncallType;
    private int mNotificationOffsetY;
    private int mNotificationPositionY;
    private PorterDuffXfermode mPDXferMode;
    private Paint mPaint;
    private Paint mPaint2;
    private Rect mRectForMaskDst;
    private Rect mRectForMaskSrc;
    private Rect mRectForScreen;
    private int mScreenHeight;
    private int mScreenWidth;
    private Bitmap mViewBottom;
    private int mViewBottomY;
    private Bitmap mViewLeft;
    private Bitmap mViewMask;
    private int mViewMaskHeight;
    private Bitmap mViewRight;
    private int mViewRightX;
    private Bitmap mViewTop;
    private int mViewTopX;

    public ColorCurvedDisplayView(Context context, String color, boolean incall) {
        super(context);
        setAlpha(1.0f);
        Point point = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(point);
        this.mScreenWidth = Math.min(point.x, point.y);
        this.mScreenHeight = Math.max(point.x, point.y);
        this.mColor = color;
        if (this.mColor == null) {
            this.mColor = RED;
        }
        this.mIsIncallType = incall;
        this.mPaint = new Paint(1);
        if (this.mIsIncallType) {
            this.mPaint2 = new Paint(1);
        }
        this.mPDXferMode = new PorterDuffXfermode(Mode.MULTIPLY);
        initResource(context);
    }

    private void initResource(Context context) {
        int type;
        Resources res = context.getResources();
        HashMap<String, Integer> map = new HashMap();
        map.put(RED, Integer.valueOf(201786398));
        map.put(BLUE, Integer.valueOf(201786397));
        map.put(ORANGE, Integer.valueOf(201786399));
        if (this.mColor.equals(RED)) {
            type = ((Integer) map.get(RED)).intValue();
        } else if (this.mColor.equals(BLUE)) {
            type = ((Integer) map.get(BLUE)).intValue();
        } else if (this.mColor.equals(ORANGE)) {
            type = ((Integer) map.get(ORANGE)).intValue();
        } else {
            type = ((Integer) map.get(RED)).intValue();
        }
        TypedArray ta = getResources().obtainTypedArray(type);
        this.mEdgeIds = new int[4];
        for (int i = 0; i < 4; i++) {
            this.mEdgeIds[i] = ta.getResourceId(i, -1);
        }
        ta.recycle();
        this.mViewTop = BitmapFactory.decodeResource(res, this.mEdgeIds[0]);
        this.mViewBottom = BitmapFactory.decodeResource(res, this.mEdgeIds[1]);
        this.mViewLeft = BitmapFactory.decodeResource(res, this.mEdgeIds[2]);
        this.mViewRight = BitmapFactory.decodeResource(res, this.mEdgeIds[3]);
        if (this.mIsIncallType) {
            this.mViewMask = BitmapFactory.decodeResource(res, 201852264);
        } else {
            this.mViewMask = BitmapFactory.decodeResource(res, 201852265);
        }
        this.mViewMaskHeight = this.mViewMask.getHeight();
        this.mViewRightX = this.mScreenWidth - this.mViewRight.getWidth();
        this.mViewBottomY = this.mScreenHeight - this.mViewBottom.getHeight();
        this.mViewTopX = (this.mScreenWidth - this.mViewTop.getWidth()) / 2;
        this.mIncallPositionY1 = this.mScreenHeight;
        this.mIncallPositionY2 = this.mScreenHeight;
        this.mRectForScreen = new Rect(0, 0, this.mScreenWidth, this.mScreenHeight);
        this.mRectForMaskSrc = new Rect(0, 0, 1, this.mViewMaskHeight);
        this.mRectForMaskDst = new Rect(0, 0, this.mScreenWidth, this.mViewMaskHeight);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIsIncallType) {
            ValueAnimator incallMaskAnimator1 = ValueAnimator.ofInt(new int[]{0, INCALL_MASK_MOVE_END});
            incallMaskAnimator1.setDuration(5000);
            incallMaskAnimator1.setInterpolator(new PathInterpolator(0.37f, INCALL_MASK_BEZIER_CONTROL_ONE_Y, INCALL_MASK_BEZIER_CONTROL_TWO_X, INCALL_MASK_BEZIER_CONTROL_TWO_Y));
            incallMaskAnimator1.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    ColorCurvedDisplayView.this.mIncallPositionY1 = ColorCurvedDisplayView.this.mScreenHeight - value;
                    ColorCurvedDisplayView.this.mIncallOffsetY1 = value;
                    ColorCurvedDisplayView.this.invalidate();
                }
            });
            ValueAnimator incallFadeOutAnimator1 = ValueAnimator.ofInt(new int[]{FADE_OUT_ALPHA_START, 0});
            incallFadeOutAnimator1.setInterpolator(new PathInterpolator(INCALL_FADEOUT_BEZIER_CONTROL_ONE_X, 0.0f, INCALL_FADEOUT_BEZIER_CONTROL_TWO_X, 1.0f));
            incallFadeOutAnimator1.setDuration(2500);
            incallFadeOutAnimator1.setStartDelay(2500);
            incallFadeOutAnimator1.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ColorCurvedDisplayView.this.mPaint.setAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            });
            final AnimatorSet incallAnimatorSet1 = new AnimatorSet();
            incallAnimatorSet1.play(incallMaskAnimator1).with(incallFadeOutAnimator1);
            incallAnimatorSet1.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    incallAnimatorSet1.start();
                }

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }
            });
            ValueAnimator incallMaskAnimator2 = ValueAnimator.ofInt(new int[]{0, INCALL_MASK_MOVE_END});
            incallMaskAnimator2.setDuration(5000);
            incallMaskAnimator2.setInterpolator(new PathInterpolator(0.37f, INCALL_MASK_BEZIER_CONTROL_ONE_Y, INCALL_MASK_BEZIER_CONTROL_TWO_X, INCALL_MASK_BEZIER_CONTROL_TWO_Y));
            incallMaskAnimator2.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    ColorCurvedDisplayView.this.mIncallPositionY2 = ColorCurvedDisplayView.this.mScreenHeight - value;
                    ColorCurvedDisplayView.this.mIncallOffsetY2 = value;
                    ColorCurvedDisplayView.this.invalidate();
                }
            });
            ValueAnimator incallFadeOutAnimator2 = ValueAnimator.ofInt(new int[]{FADE_OUT_ALPHA_START, 0});
            incallFadeOutAnimator2.setInterpolator(new PathInterpolator(INCALL_FADEOUT_BEZIER_CONTROL_ONE_X, 0.0f, INCALL_FADEOUT_BEZIER_CONTROL_TWO_X, 1.0f));
            incallFadeOutAnimator2.setDuration(2500);
            incallFadeOutAnimator2.setStartDelay(2500);
            incallFadeOutAnimator2.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ColorCurvedDisplayView.this.mPaint2.setAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            });
            final AnimatorSet incallAnimatorSet2 = new AnimatorSet();
            incallAnimatorSet2.play(incallMaskAnimator2).with(incallFadeOutAnimator2);
            incallAnimatorSet2.setStartDelay(2500);
            incallAnimatorSet2.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    incallAnimatorSet2.setStartDelay(0);
                    incallAnimatorSet2.start();
                }

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }
            });
            incallAnimatorSet1.start();
            incallAnimatorSet2.start();
            return;
        }
        ValueAnimator notificationMaskAnimator = ValueAnimator.ofInt(new int[]{NOTIFICATION_MASK_MOVE_START, NOTIFICATION_MASK_MOVE_END});
        notificationMaskAnimator.setDuration(4000);
        notificationMaskAnimator.setInterpolator(new PathInterpolator(0.37f, 0.37f, NOTIFICATION_MASK_BEZIER_CONTROL_TWO_X, NOTIFICATION_MASK_BEZIER_CONTROL_TWO_Y));
        notificationMaskAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                ColorCurvedDisplayView.this.mNotificationPositionY = ColorCurvedDisplayView.this.mScreenHeight - value;
                ColorCurvedDisplayView.this.mNotificationOffsetY = value;
                ColorCurvedDisplayView.this.invalidate();
            }
        });
        ValueAnimator notificationFadeOutAnimator = ValueAnimator.ofInt(new int[]{FADE_OUT_ALPHA_START, 0});
        notificationFadeOutAnimator.setInterpolator(new PathInterpolator(NOTIFICATION_FADEOUT_BEZIER_CONTROL_ONE_X, 0.0f, NOTIFICATION_FADEOUT_BEZIER_CONTROL_TWO_X, 1.0f));
        notificationFadeOutAnimator.setDuration(2200);
        notificationFadeOutAnimator.setStartDelay(1800);
        notificationFadeOutAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorCurvedDisplayView.this.mPaint.setAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        });
        final AnimatorSet notificationAnimatorSet = new AnimatorSet();
        notificationAnimatorSet.play(notificationMaskAnimator).with(notificationFadeOutAnimator);
        notificationAnimatorSet.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                if (ColorCurvedDisplayView.this.mAnimRepeatCount < 1) {
                    notificationAnimatorSet.start();
                } else {
                    notificationAnimatorSet.cancel();
                }
                ColorCurvedDisplayView colorCurvedDisplayView = ColorCurvedDisplayView.this;
                colorCurvedDisplayView.mAnimRepeatCount = colorCurvedDisplayView.mAnimRepeatCount + 1;
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
        notificationAnimatorSet.start();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!(this.mViewTop == null || (this.mViewTop.isRecycled() ^ 1) == 0)) {
            this.mViewTop.recycle();
            this.mViewTop = null;
        }
        if (!(this.mViewBottom == null || (this.mViewBottom.isRecycled() ^ 1) == 0)) {
            this.mViewBottom.recycle();
            this.mViewTop = null;
        }
        if (!(this.mViewLeft == null || (this.mViewLeft.isRecycled() ^ 1) == 0)) {
            this.mViewLeft.recycle();
            this.mViewTop = null;
        }
        if (!(this.mViewRight == null || (this.mViewRight.isRecycled() ^ 1) == 0)) {
            this.mViewRight.recycle();
            this.mViewTop = null;
        }
        if (this.mViewMask != null && (this.mViewMask.isRecycled() ^ 1) != 0) {
            this.mViewMask.recycle();
            this.mViewMask = null;
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveCount;
        if (this.mIsIncallType) {
            saveCount = canvas.saveLayer(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), null, 31);
            if (this.mIncallOffsetY1 < this.mScreenHeight) {
                this.mRectForScreen.offsetTo(0, this.mIncallPositionY1);
            } else if (this.mIncallOffsetY1 < this.mScreenHeight || this.mIncallOffsetY1 >= this.mViewMaskHeight) {
                this.mRectForScreen.offsetTo(0, this.mViewMaskHeight - this.mIncallOffsetY1);
            } else {
                this.mRectForScreen.offsetTo(0, 0);
            }
            this.mPaint.setXfermode(null);
            canvas.clipRect(this.mRectForScreen);
            canvas.drawBitmap(this.mViewRight, (float) this.mViewRightX, 0.0f, this.mPaint);
            canvas.drawBitmap(this.mViewBottom, (float) this.mViewTopX, (float) this.mViewBottomY, this.mPaint);
            canvas.drawBitmap(this.mViewTop, (float) this.mViewTopX, 0.0f, this.mPaint);
            canvas.drawBitmap(this.mViewLeft, 0.0f, 0.0f, this.mPaint);
            this.mPaint.setXfermode(this.mPDXferMode);
            this.mRectForMaskDst.offsetTo(0, this.mIncallPositionY1);
            canvas.drawBitmap(this.mViewMask, this.mRectForMaskSrc, this.mRectForMaskDst, this.mPaint);
            canvas.restoreToCount(saveCount);
            int saveCount2 = canvas.saveLayer(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), null, 31);
            if (this.mIncallOffsetY2 < this.mScreenHeight) {
                this.mRectForScreen.offsetTo(0, this.mIncallPositionY2);
            } else if (this.mIncallOffsetY2 < this.mScreenHeight || this.mIncallOffsetY2 >= this.mViewMaskHeight) {
                this.mRectForScreen.offsetTo(0, this.mViewMaskHeight - this.mIncallOffsetY2);
            } else {
                this.mRectForScreen.offsetTo(0, 0);
            }
            this.mPaint2.setXfermode(null);
            canvas.clipRect(this.mRectForScreen);
            canvas.drawBitmap(this.mViewRight, (float) this.mViewRightX, 0.0f, this.mPaint2);
            canvas.drawBitmap(this.mViewBottom, (float) this.mViewTopX, (float) this.mViewBottomY, this.mPaint2);
            canvas.drawBitmap(this.mViewTop, (float) this.mViewTopX, 0.0f, this.mPaint2);
            canvas.drawBitmap(this.mViewLeft, 0.0f, 0.0f, this.mPaint2);
            this.mPaint2.setXfermode(this.mPDXferMode);
            this.mRectForMaskDst.offsetTo(0, this.mIncallPositionY2);
            canvas.drawBitmap(this.mViewMask, this.mRectForMaskSrc, this.mRectForMaskDst, this.mPaint2);
            canvas.restoreToCount(saveCount2);
            return;
        }
        saveCount = canvas.saveLayer(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), null, 31);
        Rect rect = new Rect(0, this.mNotificationPositionY, this.mScreenWidth, this.mScreenHeight);
        this.mPaint.setXfermode(null);
        canvas.clipRect(rect);
        canvas.drawBitmap(this.mViewLeft, 0.0f, 0.0f, this.mPaint);
        canvas.drawBitmap(this.mViewRight, (float) this.mViewRightX, 0.0f, this.mPaint);
        canvas.drawBitmap(this.mViewBottom, (float) this.mViewTopX, (float) this.mViewBottomY, this.mPaint);
        canvas.drawBitmap(this.mViewTop, (float) this.mViewTopX, 0.0f, this.mPaint);
        this.mPaint.setXfermode(this.mPDXferMode);
        canvas.drawBitmap(this.mViewMask, new Rect(0, 0, 1, this.mNotificationOffsetY), new Rect(0, this.mNotificationPositionY, this.mScreenWidth, this.mScreenHeight), this.mPaint);
        canvas.restoreToCount(saveCount);
    }
}

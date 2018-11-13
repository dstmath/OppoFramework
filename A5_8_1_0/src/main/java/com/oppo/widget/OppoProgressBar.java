package com.oppo.widget;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Pools.SynchronizedPool;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewDebug.ExportedProperty;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.RemoteViews.RemoteView;
import com.color.util.ColorLog;
import java.util.ArrayList;

@RemoteView
public class OppoProgressBar extends View {
    private static final int MAX_LEVEL = 10000;
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
    private AccessibilityEventSender mAccessibilityEventSender;
    private AlphaAnimation mAnimation;
    private boolean mAttached;
    private int mBehavior;
    private Drawable mCurrentDrawable;
    private int mDuration;
    private boolean mHasAnimation;
    private boolean mInDrawing;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private Interpolator mInterpolator;
    private boolean mIsCustomStyle;
    private int mMax;
    int mMaxHeight;
    int mMaxWidth;
    int mMinHeight;
    int mMinWidth;
    boolean mMirrorForRtl;
    private boolean mNoInvalidate;
    private boolean mOnlyIndeterminate;
    protected Drawable mOppoProgressBgDrawable;
    protected Drawable mOppoProgressDrawable;
    private int mProgress;
    private Drawable mProgressDrawable;
    private final ArrayList<RefreshData> mRefreshData;
    private boolean mRefreshIsPosted;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    Bitmap mSampleTile;
    private float mScale;
    private int mSecondaryProgress;
    private boolean mShouldStartAnimationDrawable;
    protected final Class<?> mTagClass;
    private Transformation mTransformation;
    private long mUiThreadId;

    private class AccessibilityEventSender implements Runnable {
        /* synthetic */ AccessibilityEventSender(OppoProgressBar this$0, AccessibilityEventSender -this1) {
            this();
        }

        private AccessibilityEventSender() {
        }

        public void run() {
            OppoProgressBar.this.sendAccessibilityEvent(4);
        }
    }

    private static class RefreshData {
        private static final int POOL_MAX = 24;
        private static final SynchronizedPool<RefreshData> sPool = new SynchronizedPool(POOL_MAX);
        public boolean fromUser;
        public int id;
        public int progress;

        private RefreshData() {
        }

        public static RefreshData obtain(int id, int progress, boolean fromUser) {
            RefreshData rd = (RefreshData) sPool.acquire();
            if (rd == null) {
                rd = new RefreshData();
            }
            rd.id = id;
            rd.progress = progress;
            rd.fromUser = fromUser;
            return rd;
        }

        public void recycle() {
            sPool.release(this);
        }
    }

    private class RefreshProgressRunnable implements Runnable {
        /* synthetic */ RefreshProgressRunnable(OppoProgressBar this$0, RefreshProgressRunnable -this1) {
            this();
        }

        private RefreshProgressRunnable() {
        }

        public void run() {
            synchronized (OppoProgressBar.this) {
                int count = OppoProgressBar.this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData rd = (RefreshData) OppoProgressBar.this.mRefreshData.get(i);
                    OppoProgressBar.this.doRefreshProgress(rd.id, rd.progress, rd.fromUser, true);
                    rd.recycle();
                }
                OppoProgressBar.this.mRefreshData.clear();
                OppoProgressBar.this.mRefreshIsPosted = false;
            }
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int progress;
        int secondaryProgress;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.progress = in.readInt();
            this.secondaryProgress = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.progress);
            out.writeInt(this.secondaryProgress);
        }
    }

    public OppoProgressBar(Context context) {
        this(context, null);
    }

    public OppoProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842871);
    }

    public OppoProgressBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public OppoProgressBar(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        boolean z = true;
        super(context, attrs, defStyle);
        this.mTagClass = getClass();
        this.mMirrorForRtl = false;
        this.mRefreshData = new ArrayList();
        this.mUiThreadId = Thread.currentThread().getId();
        initProgressBar();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyle, styleRes);
        this.mNoInvalidate = true;
        Drawable drawable = a.getDrawable(8);
        if (drawable != null) {
            setProgressDrawable(tileify(drawable, false));
        }
        this.mDuration = a.getInt(9, this.mDuration);
        this.mMinWidth = a.getDimensionPixelSize(11, this.mMinWidth);
        this.mMaxWidth = a.getDimensionPixelSize(0, this.mMaxWidth);
        this.mMinHeight = a.getDimensionPixelSize(12, this.mMinHeight);
        this.mMaxHeight = a.getDimensionPixelSize(1, this.mMaxHeight);
        this.mBehavior = a.getInt(10, this.mBehavior);
        int resID = a.getResourceId(13, 17432587);
        if (resID > 0) {
            setInterpolator(context, resID);
        }
        setMax(a.getInt(2, this.mMax));
        setProgress(a.getInt(3, this.mProgress));
        setSecondaryProgress(a.getInt(4, this.mSecondaryProgress));
        drawable = a.getDrawable(7);
        if (drawable != null) {
            setIndeterminateDrawable(tileifyIndeterminate(drawable));
        }
        this.mOnlyIndeterminate = a.getBoolean(6, this.mOnlyIndeterminate);
        this.mNoInvalidate = false;
        if (!this.mOnlyIndeterminate) {
            z = a.getBoolean(5, this.mIndeterminate);
        }
        setIndeterminate(z);
        this.mMirrorForRtl = a.getBoolean(15, this.mMirrorForRtl);
        a.recycle();
        initOppoProgressBar(context, attrs, defStyle, styleRes);
    }

    private Drawable tileify(Drawable drawable, boolean clip) {
        int i;
        if (drawable instanceof LayerDrawable) {
            LayerDrawable background = (LayerDrawable) drawable;
            int N = background.getNumberOfLayers();
            Drawable[] outDrawables = new Drawable[N];
            for (i = 0; i < N; i++) {
                int id = background.getId(i);
                Drawable drawable2 = background.getDrawable(i);
                boolean z = id == 16908301 || id == 16908303;
                outDrawables[i] = tileify(drawable2, z);
            }
            LayerDrawable newBg = new LayerDrawable(outDrawables);
            for (i = 0; i < N; i++) {
                newBg.setId(i, background.getId(i));
            }
            return newBg;
        } else if (drawable instanceof StateListDrawable) {
            StateListDrawable in = (StateListDrawable) drawable;
            StateListDrawable out = new StateListDrawable();
            int numStates = in.getStateCount();
            for (i = 0; i < numStates; i++) {
                out.addState(in.getStateSet(i), tileify(in.getStateDrawable(i), clip));
            }
            return out;
        } else if (!(drawable instanceof BitmapDrawable)) {
            return drawable;
        } else {
            Bitmap tileBitmap = ((BitmapDrawable) drawable).getBitmap();
            if (this.mSampleTile == null) {
                this.mSampleTile = tileBitmap;
            }
            ShapeDrawable shapeDrawable = new ShapeDrawable(getDrawableShape());
            shapeDrawable.getPaint().setShader(new BitmapShader(tileBitmap, TileMode.REPEAT, TileMode.CLAMP));
            if (clip) {
                shapeDrawable = new ClipDrawable(shapeDrawable, 3, 1);
            }
            return shapeDrawable;
        }
    }

    Shape getDrawableShape() {
        return new RoundRectShape(new float[]{5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f}, null, null);
    }

    private Drawable tileifyIndeterminate(Drawable drawable) {
        if (!(drawable instanceof AnimationDrawable)) {
            return drawable;
        }
        AnimationDrawable background = (AnimationDrawable) drawable;
        int N = background.getNumberOfFrames();
        Drawable newBg = new AnimationDrawable();
        newBg.setOneShot(background.isOneShot());
        for (int i = 0; i < N; i++) {
            Drawable frame = tileify(background.getFrame(i), true);
            frame.setLevel(MAX_LEVEL);
            newBg.addFrame(frame, background.getDuration(i));
        }
        newBg.setLevel(MAX_LEVEL);
        return newBg;
    }

    private void initProgressBar() {
        this.mMax = 100;
        this.mProgress = 0;
        this.mSecondaryProgress = 0;
        this.mIndeterminate = false;
        this.mOnlyIndeterminate = false;
        this.mDuration = 4000;
        this.mBehavior = 1;
        this.mMinWidth = 24;
        this.mMaxWidth = 48;
        this.mMinHeight = 24;
        this.mMaxHeight = 48;
    }

    @ExportedProperty(category = "progress")
    public synchronized boolean isIndeterminate() {
        return this.mIndeterminate;
    }

    @RemotableViewMethod
    public synchronized void setIndeterminate(boolean indeterminate) {
        if (!((this.mOnlyIndeterminate && (this.mIndeterminate ^ 1) == 0) || indeterminate == this.mIndeterminate)) {
            this.mIndeterminate = indeterminate;
            if (indeterminate) {
                this.mCurrentDrawable = this.mIndeterminateDrawable;
                startAnimation();
            } else {
                this.mCurrentDrawable = this.mProgressDrawable;
                stopAnimation();
            }
        }
    }

    public Drawable getIndeterminateDrawable() {
        return this.mIndeterminateDrawable;
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (d != null) {
            d.setCallback(this);
        }
        this.mIndeterminateDrawable = d;
        if (this.mIndeterminateDrawable != null && canResolveLayoutDirection()) {
            this.mIndeterminateDrawable.setLayoutDirection(getLayoutDirection());
        }
        if (this.mIndeterminate) {
            this.mCurrentDrawable = d;
            postInvalidate();
        }
    }

    public Drawable getProgressDrawable() {
        return this.mProgressDrawable;
    }

    public void setProgressDrawable(Drawable d) {
        boolean needUpdate;
        if (this.mProgressDrawable == null || d == this.mProgressDrawable) {
            needUpdate = false;
        } else {
            this.mProgressDrawable.setCallback(null);
            needUpdate = true;
        }
        if (d != null) {
            d.setCallback(this);
            if (canResolveLayoutDirection()) {
                d.setLayoutDirection(getLayoutDirection());
            }
            int drawableHeight = d.getMinimumHeight();
            if (this.mMaxHeight < drawableHeight) {
                this.mMaxHeight = drawableHeight;
                requestLayout();
            }
        }
        this.mProgressDrawable = d;
        if (!this.mIndeterminate) {
            this.mCurrentDrawable = d;
            postInvalidate();
        }
        if (needUpdate) {
            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();
            doRefreshProgress(16908301, this.mProgress, false, false);
            doRefreshProgress(16908303, this.mSecondaryProgress, false, false);
        }
    }

    Drawable getCurrentDrawable() {
        return this.mCurrentDrawable;
    }

    protected boolean verifyDrawable(Drawable who) {
        if (who == this.mProgressDrawable || who == this.mIndeterminateDrawable) {
            return true;
        }
        return super.verifyDrawable(who);
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.jumpToCurrentState();
        }
        if (this.mIndeterminateDrawable != null) {
            this.mIndeterminateDrawable.jumpToCurrentState();
        }
    }

    public void onResolveDrawables(int layoutDirection) {
        Drawable d = this.mCurrentDrawable;
        if (d != null) {
            d.setLayoutDirection(layoutDirection);
        }
        if (this.mIndeterminateDrawable != null) {
            this.mIndeterminateDrawable.setLayoutDirection(layoutDirection);
        }
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setLayoutDirection(layoutDirection);
        }
    }

    public void postInvalidate() {
        if (!this.mNoInvalidate) {
            super.postInvalidate();
        }
    }

    private synchronized void doRefreshProgress(int id, int progress, boolean fromUser, boolean callBackToApp) {
        float scale = this.mMax > 0 ? ((float) progress) / ((float) this.mMax) : 0.0f;
        this.mScale = scale;
        Drawable d = this.mCurrentDrawable;
        if (d != null) {
            Drawable progressDrawable = null;
            if (d instanceof LayerDrawable) {
                progressDrawable = ((LayerDrawable) d).findDrawableByLayerId(id);
                if (progressDrawable != null && canResolveLayoutDirection()) {
                    progressDrawable.setLayoutDirection(getLayoutDirection());
                }
            }
            int level = (int) (10000.0f * scale);
            if (progressDrawable == null) {
                progressDrawable = d;
            }
            progressDrawable.setLevel(level);
        } else {
            invalidate();
        }
        if (callBackToApp && id == 16908301) {
            onProgressRefresh(scale, fromUser);
        }
    }

    void onProgressRefresh(float scale, boolean fromUser) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            scheduleAccessibilityEventSender();
        }
    }

    private synchronized void refreshProgress(int id, int progress, boolean fromUser) {
        if (this.mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(id, progress, fromUser, true);
        } else {
            if (this.mRefreshProgressRunnable == null) {
                this.mRefreshProgressRunnable = new RefreshProgressRunnable(this, null);
            }
            this.mRefreshData.add(RefreshData.obtain(id, progress, fromUser));
            if (this.mAttached && (this.mRefreshIsPosted ^ 1) != 0) {
                post(this.mRefreshProgressRunnable);
                this.mRefreshIsPosted = true;
            }
        }
    }

    @RemotableViewMethod
    public synchronized void setProgress(int progress) {
        setProgress(progress, false);
    }

    /* JADX WARNING: Missing block: B:16:0x001f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @RemotableViewMethod
    synchronized void setProgress(int progress, boolean fromUser) {
        if (!this.mIndeterminate) {
            if (progress < 0) {
                progress = 0;
            }
            if (progress > this.mMax) {
                progress = this.mMax;
            }
            if (progress != this.mProgress) {
                this.mProgress = progress;
                refreshProgress(16908301, this.mProgress, fromUser);
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @RemotableViewMethod
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (!this.mIndeterminate) {
            if (secondaryProgress < 0) {
                secondaryProgress = 0;
            }
            if (secondaryProgress > this.mMax) {
                secondaryProgress = this.mMax;
            }
            if (secondaryProgress != this.mSecondaryProgress) {
                this.mSecondaryProgress = secondaryProgress;
                refreshProgress(16908303, this.mSecondaryProgress, false);
            }
        }
    }

    @ExportedProperty(category = "progress")
    public synchronized int getProgress() {
        return this.mIndeterminate ? 0 : this.mProgress;
    }

    @ExportedProperty(category = "progress")
    public synchronized int getSecondaryProgress() {
        return this.mIndeterminate ? 0 : this.mSecondaryProgress;
    }

    @ExportedProperty(category = "progress")
    public synchronized int getMax() {
        return this.mMax;
    }

    @RemotableViewMethod
    public synchronized void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != this.mMax) {
            this.mMax = max;
            postInvalidate();
            if (this.mProgress > max) {
                this.mProgress = max;
            }
            refreshProgress(16908301, this.mProgress, false);
        }
    }

    public final synchronized void incrementProgressBy(int diff) {
        setProgress(this.mProgress + diff);
    }

    public final synchronized void incrementSecondaryProgressBy(int diff) {
        setSecondaryProgress(this.mSecondaryProgress + diff);
    }

    void startAnimation() {
        if (getVisibility() == 0) {
            if (this.mIndeterminateDrawable instanceof Animatable) {
                this.mShouldStartAnimationDrawable = true;
                this.mHasAnimation = false;
            } else {
                this.mHasAnimation = true;
                if (this.mInterpolator == null) {
                    this.mInterpolator = new LinearInterpolator();
                }
                if (this.mTransformation == null) {
                    this.mTransformation = new Transformation();
                } else {
                    this.mTransformation.clear();
                }
                if (this.mAnimation == null) {
                    this.mAnimation = new AlphaAnimation(0.0f, 1.0f);
                } else {
                    this.mAnimation.reset();
                }
                this.mAnimation.setRepeatMode(this.mBehavior);
                this.mAnimation.setRepeatCount(-1);
                this.mAnimation.setDuration((long) this.mDuration);
                this.mAnimation.setInterpolator(this.mInterpolator);
                this.mAnimation.setStartTime(-1);
            }
            postInvalidate();
        }
    }

    void stopAnimation() {
        this.mHasAnimation = false;
        if (this.mIndeterminateDrawable instanceof Animatable) {
            ((Animatable) this.mIndeterminateDrawable).stop();
            this.mShouldStartAnimationDrawable = false;
        }
        postInvalidate();
    }

    public void setInterpolator(Context context, int resID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resID));
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    @RemotableViewMethod
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (!this.mIndeterminate) {
                return;
            }
            if (v == 8 || v == 4) {
                stopAnimation();
            } else {
                startAnimation();
            }
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (!this.mIndeterminate) {
            return;
        }
        if (visibility == 8 || visibility == 4) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    public void invalidateDrawable(Drawable dr) {
        if (!this.mInDrawing) {
            if (verifyDrawable(dr)) {
                Rect dirty = dr.getBounds();
                int scrollX = this.mScrollX + this.mPaddingLeft;
                int scrollY = this.mScrollY + this.mPaddingTop;
                invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
                return;
            }
            super.invalidateDrawable(dr);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateDrawableBounds(w, h);
    }

    private void updateDrawableBounds(int w, int h) {
        w -= this.mPaddingRight + this.mPaddingLeft;
        h -= this.mPaddingTop + this.mPaddingBottom;
        int right = w;
        int bottom = h;
        int top = 0;
        int left = 0;
        if (this.mIndeterminateDrawable != null) {
            if (this.mOnlyIndeterminate && ((this.mIndeterminateDrawable instanceof AnimationDrawable) ^ 1) != 0) {
                float intrinsicAspect = ((float) this.mIndeterminateDrawable.getIntrinsicWidth()) / ((float) this.mIndeterminateDrawable.getIntrinsicHeight());
                float boundAspect = ((float) w) / ((float) h);
                if (intrinsicAspect != boundAspect) {
                    if (boundAspect > intrinsicAspect) {
                        int width = (int) (((float) h) * intrinsicAspect);
                        left = (w - width) / 2;
                        right = left + width;
                    } else {
                        int height = (int) (((float) w) * (1.0f / intrinsicAspect));
                        top = (h - height) / 2;
                        bottom = top + height;
                    }
                }
            }
            if (isLayoutRtl() && this.mMirrorForRtl) {
                int tempLeft = left;
                left = w - right;
                right = w - tempLeft;
            }
            this.mIndeterminateDrawable.setBounds(left, top, right, bottom);
        }
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    /* JADX WARNING: Missing block: B:33:0x00a7, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ColorLog.d("log.key.progress_bar.draw", this.mTagClass, new Object[]{"onDraw mOnlyIndeterminate = ", Boolean.valueOf(this.mOnlyIndeterminate)});
        if ((!isOppoStyle() || (this.mOnlyIndeterminate ^ 1) == 0) && !this.mIsCustomStyle) {
            ColorLog.d("log.key.progress_bar.draw", this.mTagClass, new Object[]{"onDraw original theme!"});
            Drawable d = this.mCurrentDrawable;
            if (d != null) {
                canvas.save();
                if (isLayoutRtl() && this.mMirrorForRtl) {
                    canvas.translate((float) (getWidth() - this.mPaddingRight), (float) this.mPaddingTop);
                    canvas.scale(-1.0f, 1.0f);
                } else {
                    canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
                }
                long time = getDrawingTime();
                if (this.mHasAnimation) {
                    this.mAnimation.getTransformation(time, this.mTransformation);
                    float scale = this.mTransformation.getAlpha();
                    try {
                        this.mInDrawing = true;
                        boolean z = (int) (10000.0f * scale);
                        d.setLevel(z);
                        this.mInDrawing = z;
                        postInvalidateOnAnimation();
                    } finally {
                        this.mInDrawing = false;
                    }
                }
                d.draw(canvas);
                canvas.restore();
                if (this.mShouldStartAnimationDrawable && (d instanceof Animatable)) {
                    ((Animatable) d).start();
                    this.mShouldStartAnimationDrawable = false;
                }
            }
        } else {
            onOppoDraw(canvas);
        }
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = this.mCurrentDrawable;
        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(this.mMinHeight, Math.min(this.mMaxHeight, d.getIntrinsicHeight()));
        }
        updateDrawableState();
        setMeasuredDimension(resolveSizeAndState(dw + (this.mPaddingLeft + this.mPaddingRight), widthMeasureSpec, 0), resolveSizeAndState(dh + (this.mPaddingTop + this.mPaddingBottom), heightMeasureSpec, 0));
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState() {
        int[] state = getDrawableState();
        if (this.mProgressDrawable != null && this.mProgressDrawable.isStateful()) {
            this.mProgressDrawable.setState(state);
        }
        if (this.mIndeterminateDrawable != null && this.mIndeterminateDrawable.isStateful()) {
            this.mIndeterminateDrawable.setState(state);
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.progress = this.mProgress;
        ss.secondaryProgress = this.mSecondaryProgress;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
        setSecondaryProgress(ss.secondaryProgress);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIndeterminate) {
            startAnimation();
        }
        if (this.mRefreshData != null) {
            synchronized (this) {
                int count = this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData rd = (RefreshData) this.mRefreshData.get(i);
                    doRefreshProgress(rd.id, rd.progress, rd.fromUser, true);
                    rd.recycle();
                }
                this.mRefreshData.clear();
            }
        }
        this.mAttached = true;
    }

    protected void onDetachedFromWindow() {
        if (this.mIndeterminate) {
            stopAnimation();
        }
        if (this.mRefreshProgressRunnable != null) {
            removeCallbacks(this.mRefreshProgressRunnable);
        }
        if (this.mRefreshProgressRunnable != null && this.mRefreshIsPosted) {
            removeCallbacks(this.mRefreshProgressRunnable);
        }
        if (this.mAccessibilityEventSender != null) {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        super.onDetachedFromWindow();
        this.mAttached = false;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OppoProgressBar.class.getName());
        event.setItemCount(this.mMax);
        event.setCurrentItemIndex(this.mProgress);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OppoProgressBar.class.getName());
    }

    private void scheduleAccessibilityEventSender() {
        if (this.mAccessibilityEventSender == null) {
            this.mAccessibilityEventSender = new AccessibilityEventSender(this, null);
        } else {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        postDelayed(this.mAccessibilityEventSender, 200);
    }

    public void setOppoProgressDrawable(Drawable d) {
        this.mOppoProgressDrawable = d;
        invalidate();
    }

    public void setOppoProgressBgDrawable(Drawable d) {
        this.mOppoProgressBgDrawable = d;
        invalidate();
    }

    public void setCustomStyled(boolean isCustomStyle) {
        ColorLog.d("log.key.progress_bar.draw", this.mTagClass, new Object[]{"setCustomStyled : ", Boolean.valueOf(isCustomStyle)});
        this.mIsCustomStyle = isCustomStyle;
    }

    private void initOppoProgressBar(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        TypedArray b = context.obtainStyledAttributes(attrs, oppo.R.styleable.OppoProgressBar, defStyle, styleRes);
        this.mIsCustomStyle = b.getBoolean(0, false);
        b.recycle();
        this.mOppoProgressDrawable = getResources().getDrawable(201851929);
        this.mOppoProgressBgDrawable = getResources().getDrawable(201851930);
    }

    private void onOppoDraw(Canvas canvas) {
        ColorLog.d("log.key.progress_bar.draw", this.mTagClass, new Object[]{"onDraw oppo theme!"});
        canvas.save();
        canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
        int space = (int) (this.mScale * ((float) ((getWidth() - this.mPaddingLeft) - this.mPaddingRight)));
        int height = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
        this.mOppoProgressBgDrawable.setBounds(0, 0, (getWidth() - this.mPaddingLeft) - this.mPaddingRight, height);
        this.mOppoProgressBgDrawable.draw(canvas);
        int min = this.mOppoProgressDrawable.getIntrinsicWidth();
        if (space >= min) {
            this.mOppoProgressDrawable.setBounds(0, 0, space, height);
        } else if (space > 0) {
            this.mOppoProgressDrawable.setBounds(0, 0, min, height);
        } else {
            this.mOppoProgressDrawable.setBounds(0, 0, 0, height);
        }
        this.mOppoProgressDrawable.draw(canvas);
        canvas.restore();
    }
}

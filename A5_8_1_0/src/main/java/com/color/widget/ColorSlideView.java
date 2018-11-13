package com.color.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.PathInterpolator;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Scroller;
import com.color.util.ColorChangeTextUtil;
import java.util.ArrayList;

public class ColorSlideView extends LinearLayout {
    public static final int FULL = 3;
    public static final int HEAD = 0;
    private static final int INVALID_POINTER = -1;
    public static final int MIDDLE = 1;
    private static final String TAG = "ColorSlideView";
    public static final int TAIL = 2;
    private static final int TAN = 2;
    private static final int VERTICAL_LINE_WIDTH = 1;
    private int[] DRAWABLEIDS = new int[]{201851138, 201851139, 201851140, 201851141};
    private int mActivePointerId = -1;
    private int mAlpha = 0;
    boolean mCanCopy = true;
    boolean mCanDelete = false;
    boolean mCanRename = false;
    private Context mContext;
    private Drawable mDiver;
    private boolean mDiverEnable = false;
    private boolean mDrawItemEnable = false;
    ValueAnimator mFadeAnim;
    private int mGroupStyle = -1;
    private int mHolderWidth = 0;
    int mInitialHeight;
    private int mInitialMotionX;
    private int mInitialMotionY;
    private PathInterpolator mInterpolator;
    private boolean mIsBeingDragged = false;
    private boolean mIsUnableToDrag = false;
    int mItemCount = 0;
    private ArrayList<ColorSlideMenuItem> mItems;
    private int mLastMotionX;
    private int mLastMotionY;
    private Layout mLayout = null;
    private Paint mLinePaint;
    private int mMaximumVelocity;
    private boolean mMenuDividerEnable = true;
    private OnDeleteItemClickListener mOnDeleteItemClickListener;
    private OnSlideListener mOnSlideListener;
    private OnSlideMenuItemClickListener mOnSlideMenuItemClickListener;
    private OnSmoothScrollListener mOnSmoothScrollListener;
    private int mPaddingRight = 18;
    Paint mPaint;
    private Path mPath1;
    private Path mPathArc;
    private int mRadius = 20;
    private Scroller mScroller;
    private int mSlideBackColor;
    private Drawable mSlideColorDrawable;
    private boolean mSlideEnable = true;
    private View mSlideView;
    private Runnable mSmoothScrollRunnable;
    private String mStringDelete;
    private int mTextPadding = 0;
    private int mTouchSlop;
    private boolean mUseDefaultBackGround = true;
    private VelocityTracker mVelocityTracker = null;
    private boolean mhasStartAnimation = false;
    boolean scrollAll = false;

    public interface OnDeleteItemClickListener {
        void onDeleteItemClick();
    }

    public interface OnSlideListener {
        public static final int SLIDE_STATUS_OFF = 0;
        public static final int SLIDE_STATUS_ON = 2;
        public static final int SLIDE_STATUS_START_SCROLL = 1;

        void onSlide(View view, int i);
    }

    public interface OnSlideMenuItemClickListener {
        void onSlideMenuItemClick(ColorSlideMenuItem colorSlideMenuItem, int i);
    }

    public interface OnSmoothScrollListener {
        void onSmoothScroll(View view);
    }

    public ColorSlideView(Context context) {
        super(context);
        initView();
    }

    public ColorSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void addItem(ColorSlideMenuItem item) {
        addItem(-1, item);
    }

    public void addItem(int index, ColorSlideMenuItem item) {
        if (this.mPaint != null) {
            int width = 0;
            if (item.getText() != null) {
                width = ((int) this.mPaint.measureText((String) item.getText())) + (this.mTextPadding * 2);
            }
            if (width > item.getWidth()) {
                item.setWidth(width);
            }
        }
        if (index < 0) {
            this.mItems.add(item);
        } else {
            this.mItems.add(index, item);
        }
        itemWidthChange();
        postInvalidate();
    }

    public void setDeleteEnable(boolean enable) {
        if (this.mCanDelete != enable) {
            this.mCanDelete = enable;
            if (enable) {
                this.mItems.add(0, new ColorSlideMenuItem(this.mContext, this.mContext.getResources().getDrawable(201852213)));
                if (this.mPaint != null) {
                    ColorSlideMenuItem item = (ColorSlideMenuItem) this.mItems.get(0);
                    int width = 0;
                    if (item.getText() != null) {
                        width = ((int) this.mPaint.measureText((String) item.getText())) + (this.mTextPadding * 2);
                    }
                    if (width > item.getWidth()) {
                        item.setWidth(width);
                    }
                }
            } else {
                this.mItems.remove(0);
            }
            itemWidthChange();
        }
    }

    public void setDeleteItemText(CharSequence text) {
        if (this.mCanDelete) {
            ColorSlideMenuItem item = (ColorSlideMenuItem) this.mItems.get(0);
            item.setText(text);
            if (this.mPaint != null) {
                int width = ((int) this.mPaint.measureText((String) item.getText())) + (this.mTextPadding * 2);
                if (width > item.getWidth()) {
                    item.setWidth(width);
                    itemWidthChange();
                }
            }
        }
    }

    public void setDeleteItemText(int text) {
        setDeleteItemText(this.mContext.getText(text));
    }

    public void setMenuDividerEnable(boolean enable) {
        this.mMenuDividerEnable = enable;
    }

    public CharSequence getDeleteItemText() {
        if (this.mCanDelete) {
            return ((ColorSlideMenuItem) this.mItems.get(0)).getText();
        }
        return null;
    }

    public void removeItem(int index) {
        if (index >= 0 && index < this.mItems.size()) {
            this.mItems.remove(index);
            itemWidthChange();
        }
    }

    private void itemWidthChange() {
        this.mHolderWidth = 0;
        this.mItemCount = this.mItems.size();
        for (int i = 0; i < this.mItemCount; i++) {
            this.mHolderWidth = ((ColorSlideMenuItem) this.mItems.get(i)).getWidth() + this.mHolderWidth;
        }
        this.mHolderWidth++;
    }

    private void initView() {
        this.mContext = getContext();
        int textSize = getResources().getDimensionPixelSize(201654415);
        textSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) textSize, getResources().getConfiguration().fontScale, 2);
        this.mPaint = new TextPaint();
        this.mPaint.setColor(this.mContext.getResources().getColor(201720864));
        this.mPaint.setTextSize((float) textSize);
        this.mTextPadding = this.mContext.getResources().getDimensionPixelSize(201654459);
        this.mPaddingRight = this.mContext.getResources().getDimensionPixelSize(201654457);
        this.mRadius = this.mContext.getResources().getDimensionPixelSize(201655523);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextAlign(Align.CENTER);
        this.mItems = new ArrayList();
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledPagingTouchSlop();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mTouchSlop = 3;
        setDeleteEnable(true);
        this.mLinePaint = new TextPaint();
        this.mLinePaint.setStrokeWidth(1.0f);
        this.mLinePaint.setColor(this.mContext.getResources().getColor(201720911));
        this.mLinePaint.setAntiAlias(true);
        this.mDiver = getContext().getResources().getDrawable(201852177);
        this.mInterpolator = new PathInterpolator(0.133f, 0.0f, 0.3f, 1.0f);
        this.mScroller = new Scroller(this.mContext, this.mInterpolator);
        setOrientation(0);
        setLayoutParams(new LayoutParams(-1, -1));
        boolean z = this.scrollAll;
        itemWidthChange();
        this.mStringDelete = this.mContext.getString(201590093);
        this.mSlideBackColor = this.mContext.getResources().getColor(201720865);
        this.mSlideColorDrawable = new ColorDrawable(this.mSlideBackColor);
        this.mSlideBackColor &= 16777215;
        this.mFadeAnim = ObjectAnimator.ofInt(this.mSlideColorDrawable, "Alpha", new int[]{0, 210});
        this.mFadeAnim.setInterpolator(this.mInterpolator);
        this.mFadeAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorSlideView.this.mAlpha = ((Integer) animation.getAnimatedValue()).intValue();
            }
        });
        setWillNotDraw(false);
    }

    public void restoreLayout() {
        this.mAlpha = 0;
        this.mSlideView.setTranslationX(0.0f);
        getLayoutParams().height = this.mInitialHeight;
        setVisibility(0);
        clearAnimation();
        this.mhasStartAnimation = false;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mSlideEnable || this.mDrawItemEnable) {
            drawItemBackground(canvas);
        }
        if (this.mDiverEnable) {
            drawDiver(canvas);
        }
    }

    private void drawDiver(Canvas canvas) {
        canvas.save();
        this.mDiver.setBounds(0, getHeight() - this.mDiver.getIntrinsicHeight(), getWidth(), getHeight());
        this.mDiver.draw(canvas);
        canvas.restore();
    }

    public void setUseDefaultBackground(boolean use) {
        this.mUseDefaultBackGround = use;
    }

    public void setGroupOffset(int padding) {
        this.mPaddingRight = padding;
    }

    public void setGroupStyle(int style) {
        this.mGroupStyle = style;
        if (this.mUseDefaultBackGround) {
            setBackgroundResource(this.DRAWABLEIDS[this.mGroupStyle]);
        }
    }

    private void clipTopRound(Canvas canvas) {
        if (this.mPath1 == null) {
            this.mPath1 = new Path();
        } else {
            this.mPath1.reset();
        }
        if (getLayoutDirection() == 1) {
            this.mPath1.moveTo((float) (this.mRadius / 2), 0.0f);
            this.mPath1.lineTo(0.0f, 0.0f);
            this.mPath1.lineTo(0.0f, (float) (this.mRadius / 2));
        } else {
            this.mPath1.moveTo((float) this.mHolderWidth, (float) (this.mRadius / 2));
            this.mPath1.lineTo((float) this.mHolderWidth, 0.0f);
            this.mPath1.lineTo((float) (this.mHolderWidth - (this.mRadius / 2)), 0.0f);
        }
        this.mPath1.close();
        canvas.clipPath(this.mPath1, Op.DIFFERENCE);
        if (this.mPathArc == null) {
            this.mPathArc = new Path();
        } else {
            this.mPathArc.reset();
        }
        if (getLayoutDirection() == 1) {
            this.mPathArc.addArc(new RectF(0.0f, 0.0f, (float) this.mRadius, (float) this.mRadius), -90.0f, -180.0f);
        } else {
            this.mPathArc.addArc(new RectF((float) (this.mHolderWidth - this.mRadius), 0.0f, (float) this.mHolderWidth, (float) this.mRadius), 0.0f, -90.0f);
        }
        canvas.clipPath(this.mPathArc, Op.UNION);
    }

    private void clipBottomRound(Canvas canvas) {
        if (this.mPath1 == null) {
            this.mPath1 = new Path();
        } else {
            this.mPath1.reset();
        }
        if (getLayoutDirection() == 1) {
            this.mPath1.moveTo(0.0f, (float) (getHeight() - (this.mRadius / 2)));
            this.mPath1.lineTo(0.0f, (float) getHeight());
            this.mPath1.lineTo((float) (this.mRadius / 2), (float) getHeight());
        } else {
            this.mPath1.moveTo((float) this.mHolderWidth, (float) (getHeight() - (this.mRadius / 2)));
            this.mPath1.lineTo((float) this.mHolderWidth, (float) getHeight());
            this.mPath1.lineTo((float) (this.mHolderWidth - (this.mRadius / 2)), (float) getHeight());
        }
        this.mPath1.close();
        canvas.clipPath(this.mPath1, Op.DIFFERENCE);
        if (this.mPathArc == null) {
            this.mPathArc = new Path();
        } else {
            this.mPathArc.reset();
        }
        if (getLayoutDirection() == 1) {
            this.mPathArc.addArc(new RectF(0.0f, (float) (getHeight() - this.mRadius), (float) this.mRadius, (float) getHeight()), 90.0f, 180.0f);
        } else {
            this.mPathArc.addArc(new RectF((float) (this.mHolderWidth - this.mRadius), (float) (getHeight() - this.mRadius), (float) this.mHolderWidth, (float) getHeight()), 0.0f, 90.0f);
        }
        canvas.clipPath(this.mPathArc, Op.UNION);
    }

    void drawItemBackground(Canvas canvas) {
        if (this.mItemCount > 0) {
            canvas.save();
            int h = getHeight();
            if (this.mAlpha > 0) {
                canvas.drawColor((this.mAlpha << 24) | this.mSlideBackColor);
            }
            float translateX = (float) (getLayoutDirection() == 1 ? 0 : getWidth() - this.mHolderWidth);
            if (this.mGroupStyle >= 0 && this.mGroupStyle <= 3) {
                translateX = getLayoutDirection() == 1 ? translateX + ((float) this.mPaddingRight) : translateX - ((float) this.mPaddingRight);
            }
            canvas.translate(translateX, 0.0f);
            if (getLayoutDirection() == 1) {
                canvas.clipRect(0, 0, (-getSlideViewScrollX()) + 1, h);
            } else {
                canvas.clipRect(this.mHolderWidth - getSlideViewScrollX(), 0, this.mHolderWidth, h);
            }
            if (this.mLayout == null) {
                this.mLayout = new StaticLayout(this.mStringDelete, (TextPaint) this.mPaint, this.mHolderWidth, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            }
            int firstLine = TextUtils.unpackRangeStartFromLong(this.mLayout.getLineRangeForDraw(canvas));
            if (firstLine < 0) {
                canvas.restore();
                return;
            }
            int lbaseline = this.mLayout.getLineTop(firstLine + 1) - this.mLayout.getLineDescent(firstLine);
            FontMetrics fontMetrics = this.mPaint.getFontMetrics();
            int h2 = ((int) Math.ceil((double) fontMetrics.descent)) - ((int) Math.ceil((double) fontMetrics.ascent));
            for (int i = 0; i < this.mItemCount; i++) {
                Drawable tempDrawable = ((ColorSlideMenuItem) this.mItems.get(i)).getBackground();
                Drawable iconDrawable = ((ColorSlideMenuItem) this.mItems.get(i)).getIcon();
                int position = 0;
                for (int j = 0; j < i; j++) {
                    position += ((ColorSlideMenuItem) this.mItems.get(j)).getWidth();
                }
                int boundsLeft = position;
                int boundsRight = position + ((ColorSlideMenuItem) this.mItems.get(i)).getWidth();
                int boundsBottom = h;
                if (!isLayoutRtl()) {
                    boundsLeft = (this.mHolderWidth - position) - ((ColorSlideMenuItem) this.mItems.get(i)).getWidth();
                    boundsRight = this.mHolderWidth - position;
                    if (((ColorSlideMenuItem) this.mItems.get(i)).getText() != null) {
                        canvas.drawText((String) ((ColorSlideMenuItem) this.mItems.get(i)).getText(), (float) ((this.mHolderWidth - position) - (((ColorSlideMenuItem) this.mItems.get(i)).getWidth() / 2)), (float) (((h / 2) + lbaseline) - (h2 / 2)), this.mPaint);
                    }
                } else if (((ColorSlideMenuItem) this.mItems.get(i)).getText() != null) {
                    canvas.drawText((String) ((ColorSlideMenuItem) this.mItems.get(i)).getText(), (float) ((((ColorSlideMenuItem) this.mItems.get(i)).getWidth() / 2) + position), (float) (((h / 2) + lbaseline) - (h2 / 2)), this.mPaint);
                }
                tempDrawable.setBounds(boundsLeft, 0, boundsRight, h);
                tempDrawable.draw(canvas);
                if (iconDrawable != null) {
                    int iconWidth = iconDrawable.getIntrinsicWidth();
                    int iconHeight = iconDrawable.getIntrinsicHeight();
                    int iconLeft = boundsLeft + ((((ColorSlideMenuItem) this.mItems.get(i)).getWidth() - iconWidth) / 2);
                    int iconTop = (h - iconHeight) / 2;
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
                    iconDrawable.draw(canvas);
                }
            }
            if (getLayoutDirection() == 1) {
                canvas.drawLine((float) (-getSlideViewScrollX()), 0.0f, (float) ((-getSlideViewScrollX()) + 1), (float) getHeight(), this.mLinePaint);
            } else {
                canvas.drawLine((float) (this.mHolderWidth - getSlideViewScrollX()), 0.0f, (float) ((this.mHolderWidth - getSlideViewScrollX()) + 1), (float) getHeight(), this.mLinePaint);
            }
            if (this.mMenuDividerEnable) {
                canvas.drawLine(0.0f, (float) (getHeight() - 1), (float) this.mHolderWidth, (float) getHeight(), this.mLinePaint);
            }
            canvas.restore();
        }
    }

    public void setContentView(View view) {
        if (this.scrollAll) {
            this.mSlideView = this;
            return;
        }
        addView(view, new LayoutParams(-1, -1));
        this.mSlideView = view;
    }

    public View getContentView() {
        return this.mSlideView;
    }

    public void setSlideEnable(boolean enable) {
        this.mSlideEnable = enable;
    }

    public boolean getSlideEnable() {
        return this.mSlideEnable;
    }

    public void setDrawItemEnable(boolean enable) {
        this.mDrawItemEnable = enable;
    }

    public boolean getDrawItemEnable() {
        return this.mDrawItemEnable;
    }

    public void setDiverEnable(boolean enable) {
        this.mDiverEnable = enable;
        invalidate();
    }

    public boolean getDiverEnable() {
        return this.mDiverEnable;
    }

    public void setDiver(int diverId) {
        setDiver(getContext().getResources().getDrawable(diverId));
    }

    public void setDiver(Drawable diver) {
        if (diver != null) {
            this.mDiverEnable = true;
        } else {
            this.mDiverEnable = false;
        }
        if (this.mDiver != diver) {
            this.mDiver = diver;
            invalidate();
        }
    }

    public Drawable getDiver() {
        return this.mDiver;
    }

    public boolean hasFocusable() {
        if (getVisibility() == 0 && isFocusable()) {
            return true;
        }
        return false;
    }

    public void setOnSlideListener(OnSlideListener onSlideListener) {
        this.mOnSlideListener = onSlideListener;
    }

    public void shrink() {
        if (getSlideViewScrollX() != 0) {
            if (this.mOnSmoothScrollListener != null) {
                if (this.mSmoothScrollRunnable != null) {
                    removeCallbacks(this.mSmoothScrollRunnable);
                }
                this.mSmoothScrollRunnable = new Runnable() {
                    public void run() {
                        ColorSlideView.this.mSmoothScrollRunnable = null;
                        if (ColorSlideView.this.mOnSmoothScrollListener != null) {
                            ColorSlideView.this.mOnSmoothScrollListener.onSmoothScroll(ColorSlideView.this);
                        }
                    }
                };
                postDelayed(this.mSmoothScrollRunnable, 200);
            }
            smoothScrollTo(0, 0);
        }
    }

    public Scroller getScroll() {
        return this.mScroller;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!this.mSlideEnable) {
            return false;
        }
        int action = ev.getAction() & 255;
        if (action == 3 || action == 1) {
            this.mIsBeingDragged = false;
            this.mIsUnableToDrag = false;
            this.mActivePointerId = -1;
            return false;
        }
        int scrollX;
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (this.mIsUnableToDrag) {
                return false;
            }
        }
        if (this.scrollAll) {
            scrollX = getScrollX();
        } else {
            scrollX = this.mSlideView.getScrollX();
        }
        int x;
        switch (action) {
            case 0:
                this.mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(ev);
                x = (int) ev.getX();
                this.mInitialMotionX = x;
                this.mLastMotionX = x;
                x = (int) ev.getY();
                this.mInitialMotionY = x;
                this.mLastMotionY = x;
                this.mIsUnableToDrag = false;
                if (this.mOnSlideListener != null) {
                    this.mOnSlideListener.onSlide(this, 1);
                    break;
                }
                break;
            case 2:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != -1) {
                    int pointerIndex = ev.findPointerIndex(activePointerId);
                    int x2 = (int) ev.getX(pointerIndex);
                    int dx = x2 - this.mLastMotionX;
                    int xDiff = Math.abs(dx);
                    int y = (int) ev.getY(pointerIndex);
                    int yDiff = Math.abs(y - this.mInitialMotionY);
                    this.mLastMotionX = x2;
                    this.mLastMotionY = y;
                    if (xDiff > this.mTouchSlop && ((float) xDiff) * 0.5f > ((float) yDiff)) {
                        this.mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        if (dx > 0) {
                            x = this.mInitialMotionX + this.mTouchSlop;
                        } else {
                            x = this.mInitialMotionX - this.mTouchSlop;
                        }
                        this.mLastMotionX = x;
                        this.mLastMotionY = y;
                    } else if (yDiff > this.mTouchSlop) {
                        this.mIsUnableToDrag = true;
                    }
                    if (this.mIsBeingDragged) {
                        int newScrollX;
                        initVelocityTrackerIfNotExists();
                        this.mVelocityTracker.addMovement(ev);
                        if (Math.abs(scrollX) >= this.mHolderWidth || this.mItemCount == 1) {
                            newScrollX = scrollX - ((dx * 3) / 7);
                        } else {
                            newScrollX = scrollX - ((dx * 4) / 7);
                        }
                        if ((getLayoutDirection() != 1 && newScrollX < 0) || (getLayoutDirection() == 1 && newScrollX > 0)) {
                            newScrollX = 0;
                        } else if (Math.abs(newScrollX) > this.mHolderWidth) {
                            newScrollX = getLayoutDirection() == 1 ? -this.mHolderWidth : this.mHolderWidth;
                        }
                        if (!this.scrollAll) {
                            this.mSlideView.scrollTo(newScrollX, 0);
                            break;
                        }
                        scrollTo(newScrollX, 0);
                        break;
                    }
                }
                break;
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int scrollX;
        int actionMasked = event.getActionMasked();
        if (!this.mSlideEnable) {
            if (!this.mDrawItemEnable) {
                return false;
            }
            if (actionMasked == 0) {
                float eX = event.getX();
                if (isLayoutRtl()) {
                    if (eX > ((float) this.mHolderWidth)) {
                        return false;
                    }
                } else if (eX < ((float) (getWidth() - getSlideViewScrollX()))) {
                    return false;
                }
            }
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (this.scrollAll) {
            scrollX = getScrollX();
        } else {
            scrollX = this.mSlideView.getScrollX();
        }
        initVelocityTrackerIfNotExists();
        int x2;
        ViewParent parent;
        float offsetX;
        float offsetY;
        int newScrollX;
        OnSlideListener onSlideListener;
        switch (event.getAction()) {
            case 0:
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                }
                if (this.mOnSlideListener != null) {
                    this.mOnSlideListener.onSlide(this, 1);
                }
                this.mActivePointerId = event.getPointerId(0);
                x2 = (int) event.getX();
                this.mInitialMotionX = x2;
                this.mLastMotionX = x2;
                x2 = (int) event.getY();
                this.mInitialMotionY = x2;
                this.mLastMotionY = x2;
                parent = getParent();
                if (parent != null && this.mSlideEnable) {
                    offsetX = (float) (((ViewGroup) parent).getScrollX() - this.mLeft);
                    offsetY = (float) (((ViewGroup) parent).getScrollY() - this.mTop);
                    event.offsetLocation(-offsetX, -offsetY);
                    ((ViewGroup) parent).onTouchEvent(event);
                    event.offsetLocation(offsetX, offsetY);
                    parent.requestDisallowInterceptTouchEvent(true);
                    break;
                }
            case 1:
                if (this.mSlideEnable || !(!this.mDrawItemEnable || getSlideViewScrollX() == 0 || Math.abs(getSlideViewScrollX()) == this.mHolderWidth)) {
                    newScrollX = 0;
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                    if (initialVelocity < -1000) {
                        newScrollX = getLayoutDirection() == 1 ? 0 : this.mHolderWidth;
                    } else if (initialVelocity > 1000) {
                        newScrollX = getLayoutDirection() == 1 ? -this.mHolderWidth : 0;
                    } else if (((double) Math.abs(scrollX)) - (((double) this.mHolderWidth) * 0.5d) > 0.0d) {
                        newScrollX = getLayoutDirection() == 1 ? -this.mHolderWidth : this.mHolderWidth;
                    }
                    smoothScrollTo(newScrollX, 0);
                    if (this.mOnSlideListener != null) {
                        onSlideListener = this.mOnSlideListener;
                        if (newScrollX == 0) {
                            x2 = 0;
                        } else {
                            x2 = 2;
                        }
                        onSlideListener.onSlide(this, x2);
                    }
                }
                if (Math.abs(getSlideViewScrollX()) == this.mHolderWidth) {
                    boolean isTouchedOnMenu;
                    if (getLayoutDirection() == 1) {
                        isTouchedOnMenu = this.mInitialMotionX < this.mHolderWidth && x < this.mHolderWidth;
                    } else {
                        isTouchedOnMenu = this.mInitialMotionX > getWidth() - this.mHolderWidth && x > getWidth() - this.mHolderWidth;
                    }
                    if (isTouchedOnMenu) {
                        int i = 0;
                        while (i < this.mItemCount) {
                            boolean isTouchedOnIndexI;
                            int position = 0;
                            for (int j = 0; j < i; j++) {
                                position += ((ColorSlideMenuItem) this.mItems.get(j)).getWidth();
                            }
                            if (getLayoutDirection() == 1) {
                                isTouchedOnIndexI = this.mInitialMotionX < ((ColorSlideMenuItem) this.mItems.get(i)).getWidth() + position && x < ((ColorSlideMenuItem) this.mItems.get(i)).getWidth() + position;
                            } else {
                                isTouchedOnIndexI = this.mInitialMotionX > (getWidth() - position) - ((ColorSlideMenuItem) this.mItems.get(i)).getWidth() && x > (getWidth() - position) - ((ColorSlideMenuItem) this.mItems.get(i)).getWidth();
                            }
                            if (isTouchedOnIndexI) {
                                if (this.mCanDelete && i == 0 && (this.mhasStartAnimation ^ 1) != 0) {
                                    this.mhasStartAnimation = true;
                                    startDeleteAnimation(this.mSlideView);
                                }
                                playSoundEffect(0);
                                if (this.mOnSlideMenuItemClickListener != null) {
                                    this.mOnSlideMenuItemClickListener.onSlideMenuItemClick((ColorSlideMenuItem) this.mItems.get(i), i);
                                }
                            } else {
                                i++;
                            }
                        }
                    } else {
                        boolean neeShinkBack = this.mInitialMotionX < getWidth() - this.mHolderWidth && x < getWidth() - this.mHolderWidth && this.mInitialMotionX - x < (-this.mHolderWidth);
                        if (getLayoutDirection() == 1) {
                            neeShinkBack = this.mInitialMotionX < this.mHolderWidth && x > this.mHolderWidth && this.mInitialMotionX - x > this.mHolderWidth;
                        }
                        if (neeShinkBack) {
                            shrink();
                        }
                    }
                }
                parent = getParent();
                if (parent != null && this.mSlideEnable) {
                    offsetX = (float) (((ViewGroup) parent).getScrollX() - this.mLeft);
                    offsetY = (float) (((ViewGroup) parent).getScrollY() - this.mTop);
                    event.offsetLocation(-offsetX, -offsetY);
                    if (this.mIsBeingDragged || (getLayoutDirection() != 1 ? getSlideViewScrollX() <= 0 : getSlideViewScrollX() >= 0)) {
                        MotionEvent cancelEvent = event.copy();
                        cancelEvent.setAction(3);
                        ((ViewGroup) parent).onTouchEvent(cancelEvent);
                        Log.d(TAG, "cancel,  mIsBeingDragged: " + this.mIsBeingDragged);
                        Log.d(TAG, "getSlideViewScrollX(): " + getSlideViewScrollX());
                    } else {
                        if (parent instanceof ListView) {
                            Log.d(TAG, "up,  getTouchMode: " + ((ListView) parent).getTouchMode());
                        }
                        Log.d(TAG, "up,  mIsBeingDragged: " + this.mIsBeingDragged);
                        Log.d(TAG, "getSlideViewScrollX(): " + getSlideViewScrollX());
                        ((ViewGroup) parent).onTouchEvent(event);
                    }
                    event.offsetLocation(offsetX, offsetY);
                }
                endDrag();
                break;
            case 2:
                int deltaX = x - this.mLastMotionX;
                int deltaY = y - this.mLastMotionY;
                int pointerIndex = event.findPointerIndex(this.mActivePointerId);
                int xM = (int) event.getX(pointerIndex);
                int dx = xM - this.mLastMotionX;
                int xDiff = Math.abs(dx);
                int yM = (int) event.getY(pointerIndex);
                int yDiff = Math.abs(yM - this.mInitialMotionY);
                this.mLastMotionX = xM;
                this.mLastMotionY = yM;
                if (xDiff > 8 && ((float) xDiff) * 0.8f > ((float) yDiff)) {
                    this.mIsBeingDragged = true;
                    if (dx > 0) {
                        x2 = this.mInitialMotionX + 8;
                    } else {
                        x2 = this.mInitialMotionX - 8;
                    }
                    this.mLastMotionX = x2;
                    this.mLastMotionY = y;
                }
                if (!this.mIsBeingDragged || deltaX == 0 || !this.mSlideEnable) {
                    if (deltaY != 0) {
                        parent = getParent();
                        if (parent != null) {
                            if (!this.mIsBeingDragged && (deltaY > 4 || deltaY < -4)) {
                                parent.requestDisallowInterceptTouchEvent(false);
                                if (parent instanceof ListView) {
                                    Log.d(TAG, "slide, setTouchMode= 0");
                                    ((ListView) parent).setTouchMode(0);
                                }
                            }
                            ((View) parent).setPressed(false);
                            setPressed(false);
                            break;
                        }
                    }
                }
                if (Math.abs(scrollX) >= this.mHolderWidth || this.mItemCount == 1) {
                    newScrollX = scrollX - ((deltaX * 3) / 7);
                } else {
                    newScrollX = scrollX - ((deltaX * 4) / 7);
                }
                parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                    ((View) parent).setPressed(false);
                }
                setPressed(false);
                if ((getLayoutDirection() == 1 || newScrollX >= 0) && (getLayoutDirection() != 1 || newScrollX <= 0)) {
                    if (Math.abs(newScrollX) > this.mHolderWidth) {
                        newScrollX = getLayoutDirection() == 1 ? -this.mHolderWidth : this.mHolderWidth;
                    }
                } else {
                    newScrollX = 0;
                }
                if (this.scrollAll) {
                    scrollTo(newScrollX, 0);
                } else {
                    this.mSlideView.scrollTo(newScrollX, 0);
                }
                this.mLastMotionX = x;
                this.mLastMotionY = y;
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(event);
                }
                return true;
                break;
            case 3:
                newScrollX = 0;
                if (((double) scrollX) - (((double) this.mHolderWidth) * 0.5d) > 0.0d) {
                    newScrollX = getLayoutDirection() == 1 ? -this.mHolderWidth : this.mHolderWidth;
                }
                smoothScrollTo(newScrollX, 0);
                if (this.mOnSlideListener != null) {
                    onSlideListener = this.mOnSlideListener;
                    if (newScrollX == 0) {
                        x2 = 0;
                    } else {
                        x2 = 2;
                    }
                    onSlideListener.onSlide(this, x2);
                }
                parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                    if ((parent instanceof ListView) && ((ListView) parent).getTouchMode() < 3) {
                        offsetX = (float) (((ViewGroup) parent).getScrollX() - this.mLeft);
                        offsetY = (float) (((ViewGroup) parent).getScrollY() - this.mTop);
                        event.offsetLocation(-offsetX, -offsetY);
                        ((ViewGroup) parent).onTouchEvent(event);
                        event.offsetLocation(offsetX, offsetY);
                    }
                }
                endDrag();
                break;
        }
        this.mLastMotionX = x;
        this.mLastMotionY = y;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(event);
        }
        return true;
    }

    private void endDrag() {
        recycleVelocityTracker();
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void startDeleteAnimation(View view) {
        startDeleteAnimation(view, getLayoutDirection() == 1 ? (float) getWidth() : 0.0f, 0.0f, -((float) (getLayoutDirection() == 1 ? -getWidth() : getWidth())), 0.0f);
    }

    public void startDeleteAnimation(View view, float startDx, float startDy, float targetX, float targetY) {
        ColorDeleteAnimation rv = new ColorDeleteAnimation(view, startDx, startDy, targetX, targetY) {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (ColorSlideView.this.mOnDeleteItemClickListener != null) {
                    ColorSlideView.this.mInitialHeight = ColorSlideView.this.getMeasuredHeight();
                    ColorSlideView.this.mFadeAnim.setDuration(200);
                    ColorSlideView.this.mFadeAnim.start();
                    ColorSlideView.this.startAnimation(new ColorSlideCollapseAnimation(ColorSlideView.this) {
                        public void onItemDelete() {
                            ColorSlideView.this.mhasStartAnimation = false;
                            ColorSlideView.this.mOnDeleteItemClickListener.onDeleteItemClick();
                        }
                    });
                }
            }
        };
        rv.setDuration(200);
        rv.start();
    }

    public int getHolderWidth() {
        return this.mHolderWidth;
    }

    public void smoothScrollTo(int destX, int destY) {
        int scrollX;
        if (this.scrollAll) {
            scrollX = getScrollX();
        } else {
            scrollX = this.mSlideView.getScrollX();
        }
        int delta = destX - scrollX;
        int duration = Math.abs(delta) * 3;
        if (duration > 200) {
            duration = 200;
        }
        this.mScroller.startScroll(scrollX, 0, delta, 0, duration);
        invalidate();
    }

    public int getSlideViewScrollX() {
        if (this.scrollAll) {
            return getScrollX();
        }
        return this.mSlideView.getScrollX();
    }

    public void setSlideViewScrollX(int x) {
        if (this.scrollAll) {
            setScrollX(x);
        } else {
            this.mSlideView.setScrollX(x);
        }
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            if (this.scrollAll) {
                scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            } else {
                this.mSlideView.scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            }
            postInvalidate();
        }
    }

    public void setOnDeleteItemClickListener(OnDeleteItemClickListener l) {
        this.mOnDeleteItemClickListener = l;
    }

    public void setOnSmoothScrollListenerr(OnSmoothScrollListener l) {
        this.mOnSmoothScrollListener = l;
    }

    public void setOnSmoothScrollListener(OnSmoothScrollListener l) {
        this.mOnSmoothScrollListener = l;
    }

    public void setOnSlideMenuItemClickListener(OnSlideMenuItemClickListener l) {
        this.mOnSlideMenuItemClickListener = l;
    }
}

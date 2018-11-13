package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import java.util.LinkedList;
import java.util.List;

public final class ColorFloatingToolbarPopup {
    private static final String FLOATING_TOOLBAR_TAG = "floating_toolbar";
    private static final int MAX_OVERFLOW_SIZE = 4;
    private static final int MIN_OVERFLOW_SIZE = 2;
    private final Drawable mArrow;
    private final AnimationSet mCloseOverflowAnimation;
    private final ViewGroup mContentContainer;
    private final Context mContext;
    private final Point mCoordsOnWindow = new Point();
    private final AnimatorSet mDismissAnimation;
    private boolean mDismissed = true;
    private final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mFastOutSlowInInterpolator;
    private boolean mHidden;
    private final AnimatorSet mHideAnimation;
    private final int mIconTextSpacing;
    private ColorFloatingToolbarIndicatorPanel mIndicatorPanel;
    private final OnComputeInternalInsetsListener mInsetsComputer = new -$Lambda$Go7JgWogXNLsQcgF5KW1dT_OHSc((byte) 0, this);
    private boolean mIsOverflowOpen;
    private Drawable mLeftButtonBackground;
    private final int mLineHeight;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final Interpolator mLogAccelerateInterpolator;
    private final ViewGroup mMainPanel;
    private Size mMainPanelSize;
    private final int mMarginHorizontal;
    private final int mMarginVertical;
    private final OnClickListener mMenuItemButtonOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if ((v.getTag() instanceof MenuItem) && ColorFloatingToolbarPopup.this.mOnMenuItemClickListener != null) {
                ColorFloatingToolbarPopup.this.mOnMenuItemClickListener.onMenuItemClick((MenuItem) v.getTag());
            }
        }
    };
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private final AnimationSet mOpenOverflowAnimation;
    private boolean mOpenOverflowUpwards;
    private final Drawable mOverflow;
    private final AnimationListener mOverflowAnimationListener;
    private final ImageButton mOverflowButton;
    private final ImageButton mOverflowButtonNext;
    private final Size mOverflowButtonSize;
    private final ViewGroup mOverflowPanel;
    private Size mOverflowPanelSize;
    private final OverflowPanelViewHelper mOverflowPanelViewHelper;
    private final View mParent;
    private final PopupWindow mPopupWindow;
    private final Runnable mPreparePopupContentRTLHelper = new Runnable() {
        public void run() {
            ColorFloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
            ColorFloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
            ColorFloatingToolbarPopup.this.mContentContainer.setAlpha(1.0f);
        }
    };
    private Drawable mRightButtonBackground;
    private final AnimatorSet mShowAnimation;
    private Drawable mSingleButtonBackground;
    private final int[] mTmpCoords = new int[2];
    private final AnimatedVectorDrawable mToArrow;
    private final AnimatedVectorDrawable mToOverflow;
    private final Region mTouchableRegion = new Region();
    private int mTransitionDurationScale;
    private final Rect mViewPortOnScreen = new Rect();

    private static final class ColorFloatingToolbarIndicatorPanel {
        private final Context mContext;
        private Drawable mImageDrawableDown = this.mImageViewDown.getDrawable();
        private Drawable mImageDrawableUp = this.mImageViewUp.getDrawable();
        private final ImageView mImageView = this.mImageViewDown;
        private final ImageView mImageViewDown;
        private final ImageView mImageViewUp;
        private final ViewGroup mIndicatorView;
        private int mMinPaddingLeft;

        public ColorFloatingToolbarIndicatorPanel(Context context) {
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mIndicatorView = new LinearLayout(context);
            this.mMinPaddingLeft = context.getResources().getDimensionPixelSize(201655522);
            LayoutParams params = new LayoutParams(-2, -2);
            this.mImageViewDown = (ImageView) LayoutInflater.from(context).inflate(201917583, null);
            this.mImageViewUp = (ImageView) LayoutInflater.from(context).inflate(201917582, null);
            if (this.mImageDrawableDown == null) {
                this.mImageDrawableDown = context.getResources().getDrawable(201852202);
            }
            if (this.mImageDrawableUp == null) {
                this.mImageDrawableUp = context.getResources().getDrawable(201852203);
            }
            this.mIndicatorView.addView(this.mImageView);
        }

        public View getView() {
            return this.mIndicatorView;
        }

        public int getMinPadding() {
            return this.mMinPaddingLeft;
        }

        public void setUpDrawable() {
            this.mImageView.setImageDrawable(this.mImageDrawableUp);
        }

        public void setDownDrawable() {
            this.mImageView.setImageDrawable(this.mImageDrawableDown);
        }

        public int getDrawableWidth() {
            return this.mImageDrawableDown.getIntrinsicWidth();
        }

        public int getDrawableHeight() {
            return this.mImageDrawableDown.getIntrinsicHeight();
        }

        public Size measure() throws IllegalStateException {
            boolean z;
            if (this.mIndicatorView.getParent() == null) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkState(z);
            this.mIndicatorView.measure(0, 0);
            return new Size(this.mIndicatorView.getMeasuredWidth(), this.mIndicatorView.getMeasuredHeight());
        }
    }

    private static final class LogAccelerateInterpolator implements Interpolator {
        private static final int BASE = 100;
        private static final float LOGS_SCALE = (1.0f / computeLog(1.0f, 100));

        /* synthetic */ LogAccelerateInterpolator(LogAccelerateInterpolator -this0) {
            this();
        }

        private LogAccelerateInterpolator() {
        }

        private static float computeLog(float t, int base) {
            return (float) (1.0d - Math.pow((double) base, (double) (-t)));
        }

        public float getInterpolation(float t) {
            return 1.0f - (computeLog(1.0f - t, 100) * LOGS_SCALE);
        }
    }

    private static final class OverflowPanel extends ListView {
        private final ColorFloatingToolbarPopup mPopup;

        OverflowPanel(ColorFloatingToolbarPopup popup) {
            super(((ColorFloatingToolbarPopup) Preconditions.checkNotNull(popup)).mContext);
            this.mPopup = popup;
            setScrollBarDefaultDelayBeforeFade(ViewConfiguration.getScrollDefaultDelay() * 3);
            setScrollIndicators(3);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mPopup.mOverflowPanelSize.getHeight() - this.mPopup.mOverflowButtonSize.getHeight(), 1073741824));
        }

        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (this.mPopup.isOverflowAnimating()) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }

        protected boolean awakenScrollBars() {
            return super.awakenScrollBars();
        }
    }

    private static final class OverflowPanelViewHelper {
        private final View mCalculator = createMenuButton(null);
        private final Context mContext;
        private final int mIconTextSpacing;
        private final int mSidePadding;

        public OverflowPanelViewHelper(Context context) {
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mIconTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_menu_button_side_padding);
            this.mSidePadding = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_overflow_side_padding);
        }

        public View getView(MenuItem menuItem, int minimumWidth, View convertView) {
            Preconditions.checkNotNull(menuItem);
            if (convertView != null) {
                ColorFloatingToolbarPopup.updateMenuItemButton(convertView, menuItem, this.mIconTextSpacing);
            } else {
                convertView = createMenuButton(menuItem);
            }
            convertView.setMinimumWidth(minimumWidth);
            return convertView;
        }

        public int calculateWidth(MenuItem menuItem) {
            ColorFloatingToolbarPopup.updateMenuItemButton(this.mCalculator, menuItem, this.mIconTextSpacing);
            this.mCalculator.measure(0, 0);
            return this.mCalculator.getMeasuredWidth();
        }

        private View createMenuButton(MenuItem menuItem) {
            View button = ColorFloatingToolbarPopup.createMenuItemButton(this.mContext, menuItem, this.mIconTextSpacing);
            button.setPadding(this.mSidePadding, 0, this.mSidePadding, 0);
            return button;
        }
    }

    /* renamed from: lambda$-com_android_internal_widget_ColorFloatingToolbarPopup_5455 */
    /* synthetic */ void m50x72c381db(InternalInsetsInfo info) {
        info.contentInsets.setEmpty();
        info.visibleInsets.setEmpty();
        info.touchableRegion.set(this.mTouchableRegion);
        info.setTouchableInsets(3);
    }

    public ColorFloatingToolbarPopup(Context context, View parent) {
        this.mParent = (View) Preconditions.checkNotNull(parent);
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mContentContainer = createContentContainer(context);
        this.mPopupWindow = createPopupWindow(this.mContentContainer);
        this.mMarginHorizontal = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin);
        this.mMarginVertical = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_vertical_margin);
        this.mLineHeight = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_height);
        this.mIconTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_menu_button_side_padding);
        this.mLogAccelerateInterpolator = new LogAccelerateInterpolator();
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_slow_in);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.linear_out_slow_in);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_linear_in);
        this.mArrow = this.mContext.getResources().getDrawable(201852190, this.mContext.getTheme());
        this.mArrow.setAutoMirrored(true);
        this.mOverflow = this.mContext.getResources().getDrawable(201852189, this.mContext.getTheme());
        this.mOverflow.setAutoMirrored(true);
        this.mToArrow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow_animation, this.mContext.getTheme());
        this.mToArrow.setAutoMirrored(true);
        this.mToOverflow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow_animation, this.mContext.getTheme());
        this.mToOverflow.setAutoMirrored(true);
        this.mLeftButtonBackground = this.mContext.getDrawable(201852200);
        this.mLeftButtonBackground.setAutoMirrored(true);
        this.mRightButtonBackground = this.mContext.getDrawable(201852201);
        this.mRightButtonBackground.setAutoMirrored(true);
        this.mSingleButtonBackground = this.mContext.getDrawable(201852206);
        this.mOverflowButtonNext = createOverflowButton();
        Drawable overFlowNextDrawable = this.mContext.getResources().getDrawable(201852189, this.mContext.getTheme());
        overFlowNextDrawable.setAutoMirrored(true);
        this.mOverflowButtonNext.setImageDrawable(overFlowNextDrawable);
        this.mOverflowButton = createOverflowButton();
        this.mOverflowButtonSize = measure(this.mOverflowButton);
        this.mMainPanel = createMainPanel();
        this.mOverflowPanelViewHelper = new OverflowPanelViewHelper(this.mContext);
        this.mOverflowPanel = createOverflowPanel();
        this.mOverflowAnimationListener = createOverflowAnimationListener();
        this.mOpenOverflowAnimation = new AnimationSet(true);
        this.mOpenOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
        this.mCloseOverflowAnimation = new AnimationSet(true);
        this.mCloseOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
        this.mShowAnimation = createEnterAnimation(this.mContentContainer);
        this.mDismissAnimation = createExitAnimation(this.mContentContainer, 150, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorFloatingToolbarPopup.this.mPopupWindow.dismiss();
                ColorFloatingToolbarPopup.this.mContentContainer.removeAllViews();
            }
        });
        this.mHideAnimation = createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorFloatingToolbarPopup.this.mPopupWindow.dismiss();
            }
        });
    }

    public void layoutMenuItems(List<MenuItem> menuItems, OnMenuItemClickListener menuItemClickListener, int suggestedWidth) {
        this.mOnMenuItemClickListener = menuItemClickListener;
        cancelOverflowAnimations();
        clearPanels();
        menuItems = layoutMainPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
        if (!menuItems.isEmpty()) {
            layoutOverflowPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
        }
        updatePopupSize();
    }

    public void show(Rect contentRectOnScreen) {
        Preconditions.checkNotNull(contentRectOnScreen);
        if (!isShowing()) {
            this.mHidden = false;
            this.mDismissed = false;
            cancelDismissAndHideAnimations();
            cancelOverflowAnimations();
            refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
            preparePopupContent();
            showIndicatorPanel(contentRectOnScreen);
            this.mPopupWindow.showAtLocation(this.mParent, 0, this.mCoordsOnWindow.x, this.mCoordsOnWindow.y);
            setTouchableSurfaceInsetsComputer();
            runShowAnimation();
        }
    }

    public void dismiss() {
        if (!this.mDismissed) {
            this.mHidden = false;
            this.mDismissed = true;
            this.mHideAnimation.cancel();
            runDismissAnimation();
            setZeroTouchableSurface();
        }
    }

    public void hide() {
        if (isShowing()) {
            this.mHidden = true;
            runHideAnimation();
            setZeroTouchableSurface();
        }
    }

    public boolean isShowing() {
        return !this.mDismissed ? this.mHidden ^ 1 : false;
    }

    public boolean isHidden() {
        return this.mHidden;
    }

    public void updateCoordinates(Rect contentRectOnScreen) {
        Preconditions.checkNotNull(contentRectOnScreen);
        if (isShowing() && (this.mPopupWindow.isShowing() ^ 1) == 0) {
            cancelOverflowAnimations();
            refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
            preparePopupContent();
            this.mPopupWindow.update(this.mCoordsOnWindow.x, this.mCoordsOnWindow.y, this.mPopupWindow.getWidth(), this.mPopupWindow.getHeight());
        }
    }

    private void refreshCoordinatesAndOverflowDirection(Rect contentRectOnScreen) {
        int y;
        refreshViewPort();
        int x = Math.min(contentRectOnScreen.centerX() - (this.mPopupWindow.getWidth() / 2), this.mViewPortOnScreen.right - this.mPopupWindow.getWidth());
        int availableHeightAboveContent = contentRectOnScreen.top - this.mViewPortOnScreen.top;
        int availableHeightBelowContent = this.mViewPortOnScreen.bottom - contentRectOnScreen.bottom;
        int margin = this.mMarginVertical * 2;
        int toolbarHeightWithVerticalMargin = this.mLineHeight + margin;
        if (hasOverflow()) {
            int minimumOverflowHeightWithMargin = calculateOverflowHeight(2) + margin;
            int availableHeightThroughContentDown = (this.mViewPortOnScreen.bottom - contentRectOnScreen.top) + toolbarHeightWithVerticalMargin;
            int availableHeightThroughContentUp = (contentRectOnScreen.bottom - this.mViewPortOnScreen.top) + toolbarHeightWithVerticalMargin;
            if (availableHeightAboveContent >= minimumOverflowHeightWithMargin) {
                y = contentRectOnScreen.top - this.mPopupWindow.getHeight();
                this.mOpenOverflowUpwards = true;
            } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin && availableHeightThroughContentDown >= minimumOverflowHeightWithMargin) {
                y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
                this.mOpenOverflowUpwards = false;
            } else if (availableHeightBelowContent >= minimumOverflowHeightWithMargin) {
                y = contentRectOnScreen.bottom;
                this.mOpenOverflowUpwards = false;
            } else if (availableHeightBelowContent < toolbarHeightWithVerticalMargin || this.mViewPortOnScreen.height() < minimumOverflowHeightWithMargin) {
                y = this.mViewPortOnScreen.top;
                this.mOpenOverflowUpwards = false;
            } else {
                y = (contentRectOnScreen.bottom + toolbarHeightWithVerticalMargin) - this.mPopupWindow.getHeight();
                this.mOpenOverflowUpwards = true;
            }
        } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin) {
            y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
        } else if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
            y = contentRectOnScreen.bottom;
        } else if (availableHeightBelowContent >= this.mLineHeight) {
            y = contentRectOnScreen.bottom - this.mMarginVertical;
        } else {
            y = Math.max(this.mViewPortOnScreen.top, contentRectOnScreen.top - toolbarHeightWithVerticalMargin);
        }
        this.mParent.getRootView().getLocationOnScreen(this.mTmpCoords);
        int rootViewLeftOnScreen = this.mTmpCoords[0];
        int rootViewTopOnScreen = this.mTmpCoords[1];
        this.mParent.getRootView().getLocationInWindow(this.mTmpCoords);
        this.mCoordsOnWindow.set(Math.max(0, x - (rootViewLeftOnScreen - this.mTmpCoords[0])), Math.max(0, y - (rootViewTopOnScreen - this.mTmpCoords[1])));
    }

    private void runShowAnimation() {
        this.mShowAnimation.start();
    }

    private void runDismissAnimation() {
        this.mDismissAnimation.start();
    }

    private void runHideAnimation() {
        this.mHideAnimation.start();
    }

    private void cancelDismissAndHideAnimations() {
        this.mDismissAnimation.cancel();
        this.mHideAnimation.cancel();
    }

    private void cancelOverflowAnimations() {
        this.mContentContainer.clearAnimation();
        this.mMainPanel.animate().cancel();
        this.mOverflowPanel.animate().cancel();
        this.mToArrow.stop();
        this.mToOverflow.stop();
    }

    private void openOverflow() {
        float overflowButtonTargetX;
        final int targetWidth = this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth();
        int targetHeight = this.mOverflowPanelSize.getHeight();
        final int startWidth = this.mContentContainer.getWidth();
        int startHeight = this.mContentContainer.getHeight();
        float startY = this.mContentContainer.getY();
        final float left = this.mContentContainer.getX();
        float right = left + ((float) this.mContentContainer.getWidth());
        float startLeftMargin = this.mIndicatorPanel.getView().getX() - left;
        float startLeft = left;
        float value = (left + startLeftMargin) - ((((float) targetWidth) * startLeftMargin) / ((float) startWidth));
        final float targetLeft = value > 0.0f ? value : 0.0f;
        Animation widthAnimation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ColorFloatingToolbarPopup.setWidth(ColorFloatingToolbarPopup.this.mContentContainer, startWidth + ((int) (((float) (targetWidth - startWidth)) * interpolatedTime)));
                ColorFloatingToolbarPopup.this.mContentContainer.setX(left + (interpolatedTime * (targetLeft - left)));
            }
        };
        final float overflowButtonStartX = this.mOverflowButton.getX();
        if (isInRTLMode()) {
            overflowButtonTargetX = (((float) targetWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
        } else {
            overflowButtonTargetX = (overflowButtonStartX - ((float) targetWidth)) + ((float) this.mOverflowButton.getWidth());
        }
        Animation overflowButtonAnimation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int i;
                float overflowButtonX = overflowButtonStartX + ((overflowButtonTargetX - overflowButtonStartX) * interpolatedTime);
                if (ColorFloatingToolbarPopup.this.isInRTLMode()) {
                    i = 0;
                } else {
                    i = ColorFloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth;
                }
                ColorFloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
            }
        };
        widthAnimation.setInterpolator(this.mLogAccelerateInterpolator);
        widthAnimation.setDuration((long) getAdjustedDuration(250));
        overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
        overflowButtonAnimation.setDuration((long) getAdjustedDuration(250));
        this.mOpenOverflowAnimation.getAnimations().clear();
        this.mOpenOverflowAnimation.getAnimations().clear();
        this.mOpenOverflowAnimation.addAnimation(widthAnimation);
        this.mContentContainer.startAnimation(this.mOpenOverflowAnimation);
        this.mIsOverflowOpen = true;
        this.mMainPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(250).start();
    }

    private void closeOverflow() {
        float targetLeftTemp;
        float overflowButtonTargetX;
        final int targetWidth = this.mMainPanelSize.getWidth();
        final int startWidth = this.mContentContainer.getWidth();
        final float left = this.mContentContainer.getX();
        if (isInRTLMode()) {
            targetLeftTemp = (float) (this.mMarginHorizontal + (this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth() > this.mMainPanelSize.getWidth() ? ((this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth()) - this.mMainPanelSize.getWidth()) / 2 : 0));
        } else {
            targetLeftTemp = (float) this.mMarginHorizontal;
        }
        final float targetLeft = targetLeftTemp;
        float startLeft = left;
        float right = left + ((float) this.mContentContainer.getWidth());
        Animation widthAnimation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ColorFloatingToolbarPopup.setWidth(ColorFloatingToolbarPopup.this.mContentContainer, startWidth + ((int) (((float) (targetWidth - startWidth)) * interpolatedTime)));
                ColorFloatingToolbarPopup.this.mContentContainer.setX(left + (interpolatedTime * (targetLeft - left)));
                ColorFloatingToolbarPopup.this.mMainPanel.setX(0.0f);
            }
        };
        int targetHeight = this.mMainPanelSize.getHeight();
        int startHeight = this.mContentContainer.getHeight();
        float bottom = this.mContentContainer.getY() + ((float) this.mContentContainer.getHeight());
        final float overflowButtonStartX = this.mOverflowButton.getX();
        if (isInRTLMode()) {
            overflowButtonTargetX = (overflowButtonStartX - ((float) startWidth)) + ((float) this.mOverflowButton.getWidth());
        } else {
            overflowButtonTargetX = (((float) startWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
        }
        Animation overflowButtonAnimation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int i;
                float overflowButtonX = overflowButtonStartX + ((overflowButtonTargetX - overflowButtonStartX) * interpolatedTime);
                if (ColorFloatingToolbarPopup.this.isInRTLMode()) {
                    i = 0;
                } else {
                    i = ColorFloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth;
                }
                ColorFloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
            }
        };
        widthAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
        widthAnimation.setDuration((long) getAdjustedDuration(250));
        overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
        overflowButtonAnimation.setDuration((long) getAdjustedDuration(250));
        this.mCloseOverflowAnimation.getAnimations().clear();
        this.mCloseOverflowAnimation.addAnimation(widthAnimation);
        this.mCloseOverflowAnimation.addAnimation(overflowButtonAnimation);
        this.mContentContainer.startAnimation(this.mCloseOverflowAnimation);
        this.mIsOverflowOpen = false;
        this.mMainPanel.animate().alpha(1.0f).withLayer().setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100).start();
        this.mOverflowPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(150).start();
        this.mOverflowButton.setImageDrawable(this.mArrow);
    }

    private void setPanelsStatesAtRestingPosition() {
        this.mOverflowButton.setEnabled(true);
        Size containerSize;
        if (this.mIsOverflowOpen) {
            containerSize = this.mOverflowPanelSize;
            setWidth(this.mContentContainer, containerSize.getWidth() + this.mOverflowButtonSize.getWidth());
            setHeight(this.mContentContainer, containerSize.getHeight());
            this.mMainPanel.setAlpha(0.0f);
            this.mMainPanel.setVisibility(4);
            this.mOverflowPanel.setAlpha(1.0f);
            this.mOverflowPanel.setVisibility(0);
            this.mOverflowButton.setImageDrawable(this.mArrow);
            this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_close_overflow_description));
            if (isInRTLMode()) {
                this.mContentContainer.setX(this.mContentContainer.getX());
                this.mMainPanel.setX(0.0f);
                this.mOverflowButton.setX((float) containerSize.getWidth());
                this.mOverflowPanel.setX(0.0f);
            } else {
                this.mContentContainer.setX(this.mContentContainer.getX());
                this.mMainPanel.setX(-this.mContentContainer.getX());
                this.mOverflowButton.setX(0.0f);
                this.mOverflowPanel.setX((float) this.mOverflowButtonSize.getWidth());
            }
            if (this.mOpenOverflowUpwards) {
                this.mMainPanel.setY((float) (containerSize.getHeight() - this.mContentContainer.getHeight()));
                this.mOverflowButton.setY((float) (containerSize.getHeight() - this.mOverflowButtonSize.getHeight()));
                this.mOverflowPanel.setY(0.0f);
                return;
            }
            this.mMainPanel.setY(0.0f);
            this.mOverflowButton.setY(0.0f);
            this.mOverflowPanel.setY(0.0f);
            return;
        }
        containerSize = this.mMainPanelSize;
        setSize(this.mContentContainer, containerSize);
        this.mMainPanel.setAlpha(1.0f);
        this.mMainPanel.setVisibility(0);
        this.mOverflowPanel.setAlpha(0.0f);
        this.mOverflowPanel.setVisibility(4);
        this.mOverflowButton.setImageDrawable(this.mOverflow);
        this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_open_overflow_description));
        if (hasOverflow()) {
            if (isInRTLMode()) {
                this.mContentContainer.setX((float) (this.mMarginHorizontal + (this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth() > this.mMainPanelSize.getWidth() ? ((this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth()) - this.mMainPanelSize.getWidth()) / 2 : 0)));
                this.mMainPanel.setX(0.0f);
                this.mOverflowButton.setX(0.0f);
                this.mOverflowPanel.setX(0.0f);
            } else {
                this.mContentContainer.setX((float) this.mMarginHorizontal);
                this.mMainPanel.setX(0.0f);
                this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                this.mOverflowPanel.setX((float) (containerSize.getWidth() - this.mOverflowPanelSize.getWidth()));
            }
            if (this.mOpenOverflowUpwards) {
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) (containerSize.getHeight() - this.mOverflowPanelSize.getHeight()));
                return;
            }
            this.mMainPanel.setY(0.0f);
            this.mOverflowButton.setY(0.0f);
            this.mOverflowPanel.setY(0.0f);
            return;
        }
        this.mContentContainer.setX((float) this.mMarginHorizontal);
        this.mMainPanel.setX(0.0f);
        this.mMainPanel.setY(0.0f);
    }

    private void updatePopupSize() {
        int width = 0;
        int height = 0;
        if (this.mMainPanelSize != null) {
            width = Math.max(0, this.mMainPanelSize.getWidth());
            height = Math.max(0, this.mMainPanelSize.getHeight());
        }
        if (this.mOverflowPanelSize != null) {
            width = Math.max(width, this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth());
            height = Math.max(height, this.mOverflowPanelSize.getHeight());
        }
        this.mPopupWindow.setWidth((this.mMarginHorizontal * 2) + width);
        this.mPopupWindow.setHeight((this.mMarginVertical * 2) + height);
        maybeComputeTransitionDurationScale();
    }

    private void refreshViewPort() {
        this.mParent.getWindowVisibleDisplayFrame(this.mViewPortOnScreen);
    }

    private int getAdjustedToolbarWidth(int suggestedWidth) {
        int width = suggestedWidth;
        refreshViewPort();
        int maximumWidth = this.mViewPortOnScreen.width() - (this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin) * 2);
        if (suggestedWidth <= 0) {
            width = this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_preferred_width);
        }
        return Math.min(width, maximumWidth);
    }

    private void setZeroTouchableSurface() {
        this.mTouchableRegion.setEmpty();
    }

    private void setContentAreaAsTouchableSurface() {
        int width;
        int height;
        Preconditions.checkNotNull(this.mMainPanelSize);
        if (this.mIsOverflowOpen) {
            Preconditions.checkNotNull(this.mOverflowPanelSize);
            width = this.mOverflowPanelSize.getWidth() + this.mOverflowButtonSize.getWidth();
            height = this.mOverflowPanelSize.getHeight();
        } else {
            width = this.mMainPanelSize.getWidth();
            height = this.mMainPanelSize.getHeight();
        }
        this.mTouchableRegion.set((int) this.mContentContainer.getX(), (int) this.mContentContainer.getY(), ((int) this.mContentContainer.getX()) + width, ((int) this.mContentContainer.getY()) + height);
    }

    private void setTouchableSurfaceInsetsComputer() {
        ViewTreeObserver viewTreeObserver = this.mPopupWindow.getContentView().getRootView().getViewTreeObserver();
        viewTreeObserver.removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        viewTreeObserver.addOnComputeInternalInsetsListener(this.mInsetsComputer);
    }

    private boolean isInRTLMode() {
        if (this.mContext.getApplicationInfo().hasRtlSupport()) {
            return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
        } else {
            return false;
        }
    }

    private boolean hasOverflow() {
        return this.mOverflowPanelSize != null;
    }

    public List<MenuItem> layoutMainPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
        MenuItem menuItem;
        Preconditions.checkNotNull(menuItems);
        int availableWidth = toolbarWidth;
        LinkedList<MenuItem> remainingMenuItems = new LinkedList();
        LinkedList<MenuItem> overflowMenuItems = new LinkedList();
        for (MenuItem menuItem2 : menuItems) {
            if (menuItem2.requiresOverflow()) {
                overflowMenuItems.add(menuItem2);
            } else {
                remainingMenuItems.add(menuItem2);
            }
        }
        remainingMenuItems.addAll(overflowMenuItems);
        this.mMainPanel.removeAllViews();
        this.mMainPanel.setPaddingRelative(0, 0, 0, 0);
        int lastGroupId = -1;
        boolean isFirstItem = true;
        while (!remainingMenuItems.isEmpty()) {
            menuItem2 = (MenuItem) remainingMenuItems.peek();
            if (!isFirstItem && menuItem2.requiresOverflow()) {
                break;
            }
            View menuItemButton = createMenuItemButton(this.mContext, menuItem2, this.mIconTextSpacing);
            if (isFirstItem) {
                menuItemButton.setPaddingRelative((int) (((double) menuItemButton.getPaddingStart()) * 1.5d), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                if (menuItems.size() > 1) {
                    menuItemButton.setBackgroundDrawable(this.mLeftButtonBackground);
                }
            }
            boolean isLastItem = remainingMenuItems.size() == 1;
            if (isLastItem) {
                menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), (int) (((double) menuItemButton.getPaddingEnd()) * 1.5d), menuItemButton.getPaddingBottom());
            }
            menuItemButton.measure(0, 0);
            int menuItemButtonWidth = Math.min(menuItemButton.getMeasuredWidth(), toolbarWidth);
            boolean isNewGroup = (isFirstItem || lastGroupId == menuItem2.getGroupId()) ? false : true;
            int extraPadding = isNewGroup ? menuItemButton.getPaddingEnd() * 2 : 0;
            boolean canFitWithOverflow = menuItemButtonWidth <= (availableWidth - this.mOverflowButtonSize.getWidth()) - extraPadding;
            boolean canFitNoOverflow = isLastItem && menuItemButtonWidth <= availableWidth - extraPadding;
            if (canFitNoOverflow && menuItems.size() > 1) {
                menuItemButton.setBackgroundDrawable(this.mRightButtonBackground);
            }
            if (menuItems.size() == 1) {
                menuItemButton.setBackgroundDrawable(this.mSingleButtonBackground);
                menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), 0, menuItemButton.getPaddingEnd(), 0);
            }
            if (!canFitWithOverflow && !canFitNoOverflow) {
                break;
            }
            if (isNewGroup) {
                View divider = createDivider(this.mContext);
                int dividerWidth = divider.getLayoutParams().width;
                View previousButton = this.mMainPanel.getChildAt(this.mMainPanel.getChildCount() - 1);
                previousButton.setPaddingRelative(previousButton.getPaddingStart(), previousButton.getPaddingTop(), (previousButton.getPaddingEnd() + (extraPadding / 2)) - dividerWidth, previousButton.getPaddingBottom());
                ViewGroup.LayoutParams prevParams = previousButton.getLayoutParams();
                prevParams.width += (extraPadding / 2) - dividerWidth;
                previousButton.-wrap18(prevParams);
                menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart() + (extraPadding / 2), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                this.mMainPanel.addView(divider);
            }
            setButtonTagAndClickListener(menuItemButton, menuItem2);
            menuItemButton.setTooltipText(menuItem2.getTooltipText());
            this.mMainPanel.addView(menuItemButton);
            ViewGroup.LayoutParams params = menuItemButton.getLayoutParams();
            params.width = (extraPadding / 2) + menuItemButtonWidth;
            menuItemButton.-wrap18(params);
            availableWidth -= menuItemButtonWidth + extraPadding;
            remainingMenuItems.pop();
            lastGroupId = menuItem2.getGroupId();
            isFirstItem = false;
        }
        if (!remainingMenuItems.isEmpty()) {
            this.mMainPanel.setPaddingRelative(0, 0, this.mOverflowButtonSize.getWidth(), 0);
        }
        this.mMainPanelSize = measure(this.mMainPanel);
        return remainingMenuItems;
    }

    private void preparePopupContent() {
        this.mContentContainer.removeAllViews();
        if (hasOverflow()) {
            this.mContentContainer.addView(this.mOverflowPanel);
        }
        this.mContentContainer.addView(this.mMainPanel);
        if (hasOverflow()) {
            this.mContentContainer.addView(this.mOverflowButton);
        }
        setPanelsStatesAtRestingPosition();
        setContentAreaAsTouchableSurface();
        if (isInRTLMode()) {
            this.mContentContainer.setAlpha(0.0f);
            this.mContentContainer.post(this.mPreparePopupContentRTLHelper);
        }
    }

    private void clearPanels() {
        this.mOverflowPanelSize = null;
        this.mMainPanelSize = null;
        this.mIsOverflowOpen = false;
        this.mMainPanel.removeAllViews();
        this.mContentContainer.removeAllViews();
    }

    private void positionContentYCoordinatesIfOpeningOverflowUpwards() {
        if (this.mOpenOverflowUpwards) {
            this.mMainPanel.setY((float) (this.mContentContainer.getHeight() - this.mMainPanelSize.getHeight()));
            this.mOverflowButton.setY((float) (this.mContentContainer.getHeight() - this.mOverflowButton.getHeight()));
            this.mOverflowPanel.setY((float) (this.mContentContainer.getHeight() - this.mOverflowPanelSize.getHeight()));
        }
    }

    private int getOverflowWidth() {
        return 0;
    }

    private int calculateOverflowHeight(int maxItemSize) {
        return getLineHeight(this.mContext);
    }

    private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
        menuItemButton.setTag(menuItem);
        menuItemButton.setOnClickListener(this.mMenuItemButtonOnClickListener);
    }

    private int getAdjustedDuration(int originalDuration) {
        if (this.mTransitionDurationScale < 150) {
            return Math.max(originalDuration - 50, 0);
        }
        if (this.mTransitionDurationScale > 300) {
            return originalDuration + 50;
        }
        return (int) (((float) originalDuration) * ValueAnimator.getDurationScale());
    }

    private void maybeComputeTransitionDurationScale() {
        if (this.mMainPanelSize != null && this.mOverflowPanelSize != null) {
            int w = this.mMainPanelSize.getWidth() - this.mOverflowPanelSize.getWidth();
            int h = this.mOverflowPanelSize.getHeight() - this.mMainPanelSize.getHeight();
            this.mTransitionDurationScale = (int) (Math.sqrt((double) ((w * w) + (h * h))) / ((double) this.mContentContainer.getContext().getResources().getDisplayMetrics().density));
        }
    }

    private ViewGroup createMainPanel() {
        ViewGroup mainPanel = new LinearLayout(this.mContext) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (ColorFloatingToolbarPopup.this.isOverflowAnimating()) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(ColorFloatingToolbarPopup.this.mMainPanelSize.getWidth(), 1073741824);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return ColorFloatingToolbarPopup.this.isOverflowAnimating();
            }
        };
        ((LinearLayout) mainPanel).setDividerDrawable(this.mContext.getDrawable(201852173));
        return mainPanel;
    }

    private ImageButton createOverflowButton() {
        ImageButton overflowButton = (ImageButton) LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_button, null);
        overflowButton.setImageDrawable(this.mOverflow);
        overflowButton.setOnClickListener(new -$Lambda$JR6kTXlwbc-f1WhiuHJPXB5zT48(this));
        return overflowButton;
    }

    /* renamed from: lambda$-com_android_internal_widget_ColorFloatingToolbarPopup_65057 */
    /* synthetic */ void m51xe5bb32d7(View v) {
        if (this.mIsOverflowOpen) {
            closeOverflow();
        } else {
            openOverflow();
        }
    }

    private boolean isOverflowAnimating() {
        int overflowOpening;
        boolean overflowClosing;
        if (this.mOpenOverflowAnimation.hasStarted()) {
            overflowOpening = this.mOpenOverflowAnimation.hasEnded() ^ 1;
        } else {
            overflowOpening = 0;
        }
        if (this.mCloseOverflowAnimation.hasStarted()) {
            overflowClosing = this.mCloseOverflowAnimation.hasEnded() ^ 1;
        } else {
            overflowClosing = false;
        }
        return overflowOpening == 0 ? overflowClosing : true;
    }

    private AnimationListener createOverflowAnimationListener() {
        return new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                ColorFloatingToolbarPopup.this.mOverflowButton.setEnabled(false);
                ColorFloatingToolbarPopup.this.mMainPanel.setVisibility(0);
                ColorFloatingToolbarPopup.this.mOverflowPanel.setVisibility(0);
            }

            public void onAnimationEnd(Animation animation) {
                ColorFloatingToolbarPopup.this.mContentContainer.post(new -$Lambda$5qwWzot5fpA60Kn5ry4ls-knvBk((byte) 0, this));
            }

            /* renamed from: lambda$-com_android_internal_widget_ColorFloatingToolbarPopup$10_68440 */
            /* synthetic */ void m52x2819c6af() {
                ColorFloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                ColorFloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
            }

            public void onAnimationRepeat(Animation animation) {
            }
        };
    }

    private static Size measure(View view) {
        boolean z;
        if (view.getParent() == null) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkState(z);
        view.measure(0, 0);
        return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    private static void setSize(View view, int width, int height) {
        view.setMinimumWidth(width);
        view.setMinimumHeight(height);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(0, 0);
        }
        params.width = width;
        params.height = height;
        view.-wrap18(params);
    }

    private static void setSize(View view, Size size) {
        setSize(view, size.getWidth(), size.getHeight());
    }

    private static void setWidth(View view, int width) {
        setSize(view, width, view.getLayoutParams().height);
    }

    private static void setHeight(View view, int height) {
        setSize(view, view.getLayoutParams().width, height);
    }

    private ViewGroup createOverflowPanel() {
        ViewGroup overflowPanel = new LinearLayout(this.mContext) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (ColorFloatingToolbarPopup.this.isOverflowAnimating()) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(ColorFloatingToolbarPopup.this.mOverflowPanelSize.getWidth(), 1073741824);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return ColorFloatingToolbarPopup.this.isOverflowAnimating();
            }
        };
        ((LinearLayout) overflowPanel).setDividerDrawable(this.mContext.getDrawable(201852173));
        ((LinearLayout) overflowPanel).setShowDividers(7);
        return overflowPanel;
    }

    private void layoutOverflowPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
        Preconditions.checkNotNull(menuItems);
        int availableWidth = toolbarWidth;
        LinkedList<MenuItem> remainingMenuItems = new LinkedList(menuItems);
        this.mOverflowPanel.removeAllViews();
        this.mOverflowPanel.setPaddingRelative(0, 0, 0, 0);
        boolean isFirstItem = true;
        while (!remainingMenuItems.isEmpty()) {
            MenuItem menuItem = (MenuItem) remainingMenuItems.peek();
            View menuItemButton = createMenuItemButton(this.mContext, menuItem, this.mIconTextSpacing);
            if (isFirstItem) {
                menuItemButton.setPaddingRelative((int) (((double) menuItemButton.getPaddingStart()) * 1.5d), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                isFirstItem = false;
            }
            if (remainingMenuItems.size() == 1) {
                menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), (int) (((double) menuItemButton.getPaddingEnd()) * 1.5d), menuItemButton.getPaddingBottom());
            }
            menuItemButton.measure(0, 0);
            int menuItemButtonWidth = Math.min(menuItemButton.getMeasuredWidth(), toolbarWidth);
            boolean canFitWithOverflow = menuItemButtonWidth <= availableWidth - this.mOverflowButtonSize.getWidth();
            boolean canFitNoOverflow = remainingMenuItems.size() == 1 && menuItemButtonWidth <= availableWidth;
            if (!canFitWithOverflow && !canFitNoOverflow) {
                this.mOverflowPanel.setPaddingRelative(0, 0, this.mOverflowButtonSize.getWidth(), 0);
                break;
            }
            setButtonTagAndClickListener(menuItemButton, menuItem);
            this.mOverflowPanel.addView(menuItemButton);
            ViewGroup.LayoutParams params = menuItemButton.getLayoutParams();
            params.width = menuItemButtonWidth;
            menuItemButton.-wrap18(params);
            availableWidth -= menuItemButtonWidth;
            remainingMenuItems.pop();
        }
        this.mOverflowButtonNext.setEnabled(false);
        this.mOverflowPanel.addView(this.mOverflowButtonNext);
        this.mOverflowPanelSize = measure(this.mOverflowPanel);
    }

    private void showIndicatorPanel(Rect contentRectOnScreen) {
        if (this.mMainPanel != null && this.mMainPanel.getChildCount() != 0) {
            ViewGroup parent = (ViewGroup) this.mContentContainer.getParent();
            if (this.mIndicatorPanel == null) {
                this.mIndicatorPanel = new ColorFloatingToolbarIndicatorPanel(this.mContext);
            }
            if (this.mIndicatorPanel.getView().getParent() == null) {
                this.mIndicatorPanel.getView().-wrap18(new LayoutParams(-2, -2));
                parent.addView(this.mIndicatorPanel.getView());
            }
            if (hasOverflow()) {
                ((LinearLayout) this.mMainPanel).setShowDividers(6);
            } else {
                ((LinearLayout) this.mMainPanel).setShowDividers(2);
            }
            positionIndicationPanel(contentRectOnScreen);
        }
    }

    private void positionIndicationPanel(Rect contentRectOnScreen) {
        Preconditions.checkNotNull(this.mIndicatorPanel);
        Preconditions.checkNotNull(this.mMainPanel);
        LayoutParams params = (LayoutParams) this.mIndicatorPanel.getView().getLayoutParams();
        this.mParent.getRootView().getLocationOnScreen(this.mTmpCoords);
        int rootViewLeftOnScreen = this.mTmpCoords[0];
        this.mParent.getRootView().getLocationInWindow(this.mTmpCoords);
        params.leftMargin = ((contentRectOnScreen.centerX() - this.mCoordsOnWindow.x) - (rootViewLeftOnScreen - this.mTmpCoords[0])) - (this.mIndicatorPanel.getDrawableWidth() / 2);
        if (params.leftMargin < this.mIndicatorPanel.getMinPadding()) {
            params.leftMargin = this.mIndicatorPanel.getMinPadding();
        }
        params.rightMargin = (this.mPopupWindow.getWidth() - params.leftMargin) - this.mIndicatorPanel.getDrawableWidth();
        LayoutParams paramsContainer = (LayoutParams) this.mContentContainer.getLayoutParams();
        this.mIndicatorPanel.getView().-wrap18(params);
        ViewGroup parent;
        if (this.mCoordsOnWindow.y <= contentRectOnScreen.top) {
            parent = (ViewGroup) this.mContentContainer.getParent();
            parent.removeAllViews();
            parent.addView(this.mContentContainer);
            parent.addView(this.mIndicatorPanel.getView());
            this.mIndicatorPanel.setDownDrawable();
            paramsContainer.topMargin = this.mMarginVertical;
        } else {
            parent = (ViewGroup) this.mContentContainer.getParent();
            parent.removeAllViews();
            parent.addView(this.mIndicatorPanel.getView());
            parent.addView(this.mContentContainer);
            this.mIndicatorPanel.setUpDrawable();
            paramsContainer.topMargin = 0;
        }
        this.mContentContainer.-wrap18(paramsContainer);
    }

    private float getOverFlowPanelPositionInParent() {
        Preconditions.checkNotNull(this.mIndicatorPanel);
        return Math.max(0.0f, Math.min((float) (((this.mCoordsOnWindow.x + ((LayoutParams) this.mIndicatorPanel.getView().getLayoutParams()).leftMargin) + (this.mIndicatorPanel.getDrawableWidth() / 2)) - (this.mContentContainer.getWidth() / 2)), (float) ((this.mViewPortOnScreen.right - this.mContentContainer.getWidth()) - this.mMarginHorizontal))) - ((float) this.mCoordsOnWindow.x);
    }

    private static ViewGroup createContentContainer(Context context) {
        ViewGroup contentContainer = (ViewGroup) LayoutInflater.from(context).inflate((int) R.layout.floating_popup_container, null);
        contentContainer.-wrap18(new ViewGroup.LayoutParams(-2, -2));
        contentContainer.setTag("floating_toolbar");
        return contentContainer;
    }

    private static PopupWindow createPopupWindow(ViewGroup content) {
        View popupContentHolder = new LinearLayout(content.getContext());
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        ((LinearLayout) popupContentHolder).setOrientation(1);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(1005);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        content.-wrap18(new ViewGroup.LayoutParams(-2, -2));
        popupContentHolder.addView(content);
        return popupWindow;
    }

    private static AnimatorSet createEnterAnimation(View view) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 1.0f}).setDuration(0);
        animation.playTogether(animatorArr);
        return animation;
    }

    private static AnimatorSet createExitAnimation(View view, int startDelay, AnimatorListener listener) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{1.0f, 0.0f}).setDuration(0);
        animation.playTogether(animatorArr);
        animation.addListener(listener);
        return animation;
    }

    private static View createMenuItemButton(Context context, MenuItem menuItem, int iconTextSpacing) {
        View menuItemButton = LayoutInflater.from(context).inflate((int) R.layout.floating_popup_menu_button, null);
        if (menuItem != null) {
            updateMenuItemButton(menuItemButton, menuItem, iconTextSpacing);
        }
        return menuItemButton;
    }

    private static View createDivider(Context context) {
        View divider = new View(context);
        int _1dp = (int) TypedValue.applyDimension(1, 1.0f, context.getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(_1dp, -1);
        params.setMarginsRelative(0, _1dp * 10, 0, _1dp * 10);
        divider.-wrap18(params);
        TypedArray a = context.obtainStyledAttributes(new TypedValue().data, new int[]{R.attr.floatingToolbarDividerColor});
        divider.setBackgroundColor(a.getColor(0, 0));
        a.recycle();
        divider.setImportantForAccessibility(2);
        divider.setEnabled(false);
        divider.setFocusable(false);
        divider.setContentDescription(null);
        return divider;
    }

    private static void updateMenuItemButton(View menuItemButton, MenuItem menuItem, int iconTextSpacing) {
        TextView buttonText = (TextView) menuItemButton.findViewById(R.id.floating_toolbar_menu_item_text);
        if (TextUtils.isEmpty(menuItem.getTitle())) {
            buttonText.setVisibility(8);
        } else {
            buttonText.setVisibility(0);
            buttonText.setText(menuItem.getTitle());
        }
        ImageView buttonIcon = (ImageView) menuItemButton.findViewById(R.id.floating_toolbar_menu_item_image);
        if (menuItem.getIcon() == null) {
            buttonIcon.setVisibility(8);
            if (buttonText != null) {
                buttonText.setPaddingRelative(0, 0, 0, 0);
            }
        } else {
            buttonIcon.setVisibility(0);
            buttonIcon.setImageDrawable(menuItem.getIcon());
            if (buttonText != null) {
                buttonText.setPaddingRelative(iconTextSpacing, 0, 0, 0);
            }
        }
        CharSequence contentDescription = menuItem.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)) {
            menuItemButton.setContentDescription(menuItem.getTitle());
        } else {
            menuItemButton.setContentDescription(contentDescription);
        }
    }

    private static int getLineHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_height);
    }
}

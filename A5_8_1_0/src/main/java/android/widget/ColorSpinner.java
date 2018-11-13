package android.widget;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.IntProperty;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import com.android.internal.view.menu.ShowableListMenu;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import com.color.view.animation.ColorPathInterpolator;
import com.color.widget.ColorSpinnerCallback;
import com.color.widget.ColorSpinnerCallback.DropdownDismissCallback;
import com.color.widget.ColorSpinnerCallback.DropdownDismissListener;

public class ColorSpinner extends Spinner implements ColorSpinnerCallback {
    private static final long ANIM_DURATION = 300;
    private static final Interpolator ANIM_INTERPOLATOR_PATH = ColorPathInterpolator.create();
    private static final Interpolator ANIM_INTERPOLATOR_POPUP = ANIM_INTERPOLATOR_PATH;
    private static final Interpolator ANIM_INTERPOLATOR_ROTATE = ANIM_INTERPOLATOR_PATH;
    private static final boolean DBG = true;
    private static final int MAX_ALPHA = 255;
    private static final float MAX_LEVEL = 10000.0f;
    private static final int MODE_THEME = -1;
    private static final String TAG = "ColorSpinner";
    private AnimatorSet mAnimatorSet;
    private boolean mDismissByDetachWindow;
    private int mDropDownHeight;
    private DropdownDismissCallback mDropdownDismissCallback;
    private RotateDrawable mExpandIcon;
    private int mExpandIconMargin;
    private int mFocusColor;
    private boolean mIsContentMeasured;
    private boolean mNeedFireOnSelected;
    private boolean mShowByRestoreState;
    private final Rect mTempRect;
    private int mTextMinWidth;
    private float mTextSize;
    private TextView mTextView;
    private boolean mUpdateSelectionAfterAnim;
    private Typeface typefaceMedium;

    private class SpinnerDropdownPopup extends DropdownPopup implements DropdownDismissListener {
        private static final int NO_ANIMATION_STYLE = 0;
        private final IntProperty<Drawable> DRAWABLE_ALPHA = new IntProperty<Drawable>("alpha") {
            public void setValue(Drawable object, int value) {
                object.setAlpha(value);
            }

            public Integer get(Drawable object) {
                return Integer.valueOf(object.getAlpha());
            }
        };
        private final int mBackgroundAlpha;
        private final ColorDrawable mBackgroundDrawable = new ColorDrawable();
        private ColorPopupWindow mDismissPopup = null;
        private int mItemClickPosition = -1;
        private boolean mNeedDelayDismiss = false;
        private final PopupTouchInterceptor mPopupTouchInterceptor = new SpinnerPopupTouchInterceptor(this, null);

        private class AnimBackgroundAlphaListener extends AnimatorListenerAdapter {
            private final Drawable mBackground;
            private final int mEndValue;

            public AnimBackgroundAlphaListener(Drawable background, int endValue) {
                this.mBackground = background;
                this.mEndValue = endValue;
            }

            public void onAnimationEnd(Animator animation) {
                this.mBackground.setAlpha(this.mEndValue);
            }
        }

        private class AnimListTranslationYListener extends AnimatorListenerAdapter {
            private final ListView mListView;

            public AnimListTranslationYListener(ListView listView) {
                this.mListView = listView;
            }

            public void onAnimationEnd(Animator animation) {
                this.mListView.setTranslationY(0.0f);
            }
        }

        private class AnimSetListener extends AnimatorListenerAdapter {
            private final ColorPopupWindow mPopup;

            public AnimSetListener(ColorPopupWindow popup) {
                this.mPopup = popup;
            }

            public void onAnimationEnd(Animator animation) {
                ColorSpinner.this.mAnimatorSet = null;
                if (this.mPopup != null) {
                    this.mPopup.superDismiss();
                }
            }
        }

        private class SpinnerPopupTouchInterceptor extends PopupTouchInterceptor {
            /* synthetic */ SpinnerPopupTouchInterceptor(SpinnerDropdownPopup this$1, SpinnerPopupTouchInterceptor -this1) {
                this();
            }

            private SpinnerPopupTouchInterceptor() {
                super();
            }

            public boolean onTouch(View v, MotionEvent event) {
                boolean result = super.onTouch(v, event);
                if (event.getAction() == 0) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    ListView listView = SpinnerDropdownPopup.this.getListView();
                    if (x < 0 || x >= SpinnerDropdownPopup.this.getViewWidth(listView) || y < 0 || y >= SpinnerDropdownPopup.this.getViewHeight(listView)) {
                        SpinnerDropdownPopup.this.dismiss();
                        return true;
                    }
                }
                return result;
            }
        }

        public SpinnerDropdownPopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Window, defStyleAttr, defStyleRes);
            this.mBackgroundAlpha = (int) (a.getFloat(0, 0.0f) * 255.0f);
            setAnchorView(ColorSpinner.this);
            a.recycle();
            setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    if (ColorSpinner.this.mOnItemClickListener != null) {
                        ColorSpinner.this.performItemClick(v, position, ColorSpinner.this.mAdapter.getItemId(position));
                    }
                    int oldPosition = ColorSpinner.this.getSelectedItemPosition();
                    SpinnerDropdownPopup.this.mNeedDelayDismiss = oldPosition != position;
                    if (ColorSpinner.this.mUpdateSelectionAfterAnim) {
                        SpinnerDropdownPopup.this.mItemClickPosition = position;
                        if (oldPosition != position) {
                            ColorSpinner.this.setNextSelectedPositionInt(position);
                            ColorSpinner.this.selectionChanged();
                            ColorSpinner.this.setNextSelectedPositionInt(oldPosition);
                        }
                    } else {
                        ColorSpinner.this.setSelection(position);
                    }
                    SpinnerDropdownPopup.this.dismiss();
                    SpinnerDropdownPopup.this.mNeedDelayDismiss = false;
                }
            });
        }

        public void show() {
            super.-wrap0();
            updatePopupWindow();
        }

        public void dismiss() {
            this.mPopup.dismiss();
        }

        public void show(int textDirection, int textAlignment) {
            boolean wasShowing = isShowing();
            computeContentWidth();
            setInputMethodMode(2);
            show();
            ListView listView = getListView();
            listView.setChoiceMode(1);
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            setSelection(ColorSpinner.this.getSelectedItemPosition());
            this.mPopup.showAsDropDown(getAnchorView(), 0, 0, 0);
            onShow();
            if (!wasShowing) {
                ViewTreeObserver vto = ColorSpinner.this.getViewTreeObserver();
                if (vto != null) {
                    final OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            if (ColorSpinner.this.-wrap2()) {
                                SpinnerDropdownPopup.this.computeContentWidth();
                                SpinnerDropdownPopup.this.show();
                                return;
                            }
                            SpinnerDropdownPopup.this.dismiss();
                        }
                    };
                    vto.addOnGlobalLayoutListener(layoutListener);
                    setOnDismissListener(new OnDismissListener() {
                        public void onDismiss() {
                            ViewTreeObserver vto = ColorSpinner.this.getViewTreeObserver();
                            if (vto != null) {
                                vto.removeOnGlobalLayoutListener(layoutListener);
                            }
                            super.-wrap3();
                        }
                    });
                }
            }
        }

        public void onPreInvokePopup(LayoutParams p) {
            p.windowAnimations = 0;
        }

        public void onAnimateDismissStart(ColorPopupWindow popup) {
            if (isShowing() && getListView() != null) {
                this.mDismissPopup = popup;
                if (ColorSpinner.this.mDropdownDismissCallback == null || !this.mNeedDelayDismiss) {
                    startDropdownDismiss();
                } else {
                    ColorSpinner.this.mDropdownDismissCallback.setDismissListener(this);
                }
            }
        }

        public void onAnimateDismissEnd(ColorPopupWindow popup) {
            if (ColorSpinner.this.mUpdateSelectionAfterAnim && this.mItemClickPosition != -1) {
                ColorSpinner.this.mNeedFireOnSelected = false;
                ColorSpinner.this.setSelection(this.mItemClickPosition);
                this.mItemClickPosition = -1;
            }
        }

        public void startDropdownDismiss() {
            onHide(this.mDismissPopup);
        }

        int buildDropDown() {
            if (ColorSpinner.this.mDropDownWidth == -1) {
                setWidth(ColorSpinner.this.getContext().getResources().getDisplayMetrics().widthPixels);
            }
            if (ColorSpinner.this.mDropDownHeight == -1) {
                setHeight(this.mPopup.getMaxAvailableHeight(getAnchorView(), getVerticalOffset(), false));
            }
            return super.buildDropDown();
        }

        private int getViewWidth(View view) {
            int width = view.getWidth();
            if (width == 0) {
                return view.getMeasuredWidth();
            }
            return width;
        }

        private int getViewHeight(View view) {
            int height = view.getHeight();
            if (height == 0) {
                return view.getMeasuredHeight();
            }
            return height;
        }

        private void updatePopupWindow() {
            this.mPopup.setTouchInterceptor(this.mPopupTouchInterceptor);
            this.mPopup.setAnimationStyle(0);
        }

        private void updateBackground() {
            this.mBackgroundDrawable.setColor(-16777216);
            this.mBackgroundDrawable.setAlpha(this.mBackgroundAlpha);
            this.mPopup.setBackgroundDrawable(this.mBackgroundDrawable);
        }

        private void updateListView() {
            ListView listView = getListView();
            ViewGroup.LayoutParams lp = listView.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(-1, -2);
            } else {
                lp.width = -1;
                lp.height = -2;
            }
            listView.-wrap18(lp);
            if (listView.getWidth() == 0 || listView.getHeight() == 0) {
                listView.measure(MeasureSpec.makeMeasureSpec(lp.width, 0), MeasureSpec.makeMeasureSpec(lp.height, Integer.MIN_VALUE));
            }
        }

        private Animator createBackgroundAnimation(Drawable background, int startValue, int endValue) {
            background.setAlpha(startValue);
            ObjectAnimator anim = ObjectAnimator.ofInt(background, this.DRAWABLE_ALPHA, new int[]{endValue});
            anim.addListener(new AnimBackgroundAlphaListener(background, endValue));
            anim.setInterpolator(ColorSpinner.ANIM_INTERPOLATOR_POPUP);
            anim.setDuration(ColorSpinner.ANIM_DURATION);
            return anim;
        }

        private Animator createBackgroundShowAnimation(Drawable background) {
            return createBackgroundAnimation(background, 0, background.getAlpha());
        }

        private Animator createBackgroundHideAnimation(Drawable background) {
            return createBackgroundAnimation(background, background.getAlpha(), 0);
        }

        private Animator createListAnimation(ListView listView, float startValue, float endValue) {
            listView.setTranslationY(startValue);
            ObjectAnimator anim = ObjectAnimator.ofFloat(listView, View.TRANSLATION_Y, new float[]{endValue});
            anim.addListener(new AnimListTranslationYListener(listView));
            anim.setInterpolator(ColorSpinner.ANIM_INTERPOLATOR_POPUP);
            anim.setDuration(ColorSpinner.ANIM_DURATION);
            return anim;
        }

        private Animator createListShowAnimation(ListView listView) {
            return createListAnimation(listView, (float) (-getViewHeight(listView)), 0.0f);
        }

        private Animator createListHideAnimation(ListView listView) {
            return createListAnimation(listView, 0.0f, (float) (-getViewHeight(listView)));
        }

        private AnimatorSet playAnimators(Animator... animators) {
            AnimatorSet animatorSet = new AnimatorSet();
            Builder builder = null;
            for (Animator anim : animators) {
                if (anim != null) {
                    if (builder == null) {
                        builder = animatorSet.play(anim);
                    } else {
                        builder.with(anim);
                    }
                }
            }
            return animatorSet;
        }

        private void onShow() {
            updatePopupWindow();
            updateBackground();
            updateListView();
            if (ColorSpinner.this.mAnimatorSet != null) {
                ColorSpinner.this.mAnimatorSet.end();
            }
            ColorSpinner.this.mAnimatorSet = playAnimators(ColorSpinner.this.createIconExpandAnimation(), createListShowAnimation(getListView()), createBackgroundShowAnimation(getBackground()));
            ColorSpinner.this.mAnimatorSet.addListener(new AnimSetListener(null));
            ColorSpinner.this.mAnimatorSet.start();
            if (ColorSpinner.this.mShowByRestoreState) {
                ColorSpinner.this.mShowByRestoreState = false;
                ColorSpinner.this.mAnimatorSet.end();
            }
        }

        private void onHide(ColorPopupWindow popup) {
            ColorPopupWindow colorPopupWindow = null;
            if (ColorSpinner.this.mAnimatorSet != null) {
                ColorSpinner.this.mAnimatorSet.end();
            }
            ColorSpinner.this.mAnimatorSet = playAnimators(ColorSpinner.this.createIconCollapseAnimation(), createListHideAnimation(getListView()), createBackgroundHideAnimation(getBackground()));
            AnimatorSet -get1 = ColorSpinner.this.mAnimatorSet;
            if (!ColorSpinner.this.mDismissByDetachWindow) {
                colorPopupWindow = popup;
            }
            -get1.addListener(new AnimSetListener(colorPopupWindow));
            ColorSpinner.this.mAnimatorSet.start();
            if (ColorSpinner.this.mDismissByDetachWindow) {
                ColorSpinner.this.mDismissByDetachWindow = false;
                popup.superDismiss();
                ColorSpinner.this.mAnimatorSet.end();
            }
        }
    }

    public ColorSpinner(Context context) {
        this(context, null);
    }

    public ColorSpinner(Context context, int mode) {
        this(context, null, com.android.internal.R.attr.spinnerStyle, mode);
    }

    public ColorSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.spinnerStyle);
    }

    public ColorSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0, -1);
    }

    public ColorSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, 0, mode);
    }

    public ColorSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        this.mTempRect = new Rect();
        this.mAnimatorSet = null;
        this.mExpandIcon = null;
        this.mDropDownHeight = 0;
        this.mExpandIconMargin = -2;
        this.mIsContentMeasured = false;
        this.mNeedFireOnSelected = true;
        this.mUpdateSelectionAfterAnim = true;
        this.mDismissByDetachWindow = false;
        this.mShowByRestoreState = false;
        this.mDropdownDismissCallback = null;
        this.typefaceMedium = null;
        this.mTextSize = 0.0f;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Spinner, defStyleAttr, defStyleRes);
        if (mode == -1) {
            mode = a.getInt(5, 0);
        }
        a.recycle();
        if (mode == 1) {
            TypedArray b = context.obtainStyledAttributes(attrs, oppo.R.styleable.ColorSpinner, defStyleAttr, defStyleRes);
            this.mDropDownHeight = b.getLayoutDimension(0, -2);
            this.mExpandIcon = (RotateDrawable) b.getDrawable(1);
            this.mExpandIconMargin = b.getDimensionPixelSize(2, 0);
            b.recycle();
            ((SpinnerDropdownPopup) this.mPopup).updateBackground();
            this.mFocusColor = ColorContextUtil.getAttrColor(context, 201392701);
        }
        this.mForwardingListener = new ForwardingListener(this) {
            public ShowableListMenu getPopup() {
                return (SpinnerDropdownPopup) ColorSpinner.this.mPopup;
            }

            public boolean onForwardingStarted() {
                return false;
            }
        };
        this.mTextSize = (float) getResources().getDimensionPixelSize(201654417);
        this.mTextSize = ColorChangeTextUtil.getSuitableFontSize(this.mTextSize, getResources().getConfiguration().fontScale, 2);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        setOnItemClickListenerInt(listener);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mExpandIcon != null && this.mIsContentMeasured) {
            int iconWidth = this.mExpandIcon.getIntrinsicWidth();
            int iconHeight = this.mExpandIcon.getIntrinsicHeight();
            -wrap6((this.mTextMinWidth + iconWidth) + this.mExpandIconMargin, getMeasuredHeight());
            boolean ltr = getLayoutDirection() == 0;
            int left = ltr ? (getMeasuredWidth() - iconWidth) - getPaddingRight() : getPaddingLeft();
            int top = getPaddingTop() + ((((getMeasuredHeight() - iconHeight) - getPaddingTop()) - getPaddingBottom()) / 2);
            this.mExpandIcon.setBounds(left, top, ltr ? left + iconWidth : iconWidth, top + iconHeight);
            this.mIsContentMeasured = false;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mNeedFireOnSelected = true;
        TextView text = (TextView) findViewById(com.android.internal.R.id.text1);
        if (text != null) {
            text.setTextColor(this.mFocusColor);
            this.mTextView = text;
            setTextSize();
        }
    }

    private void setTextSize() {
        if (this.mTextView != null) {
            this.mTextView.setTextSize(0, (float) ((int) this.mTextSize));
        }
    }

    public void setSpinnerTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mDismissByDetachWindow = false;
    }

    protected void onDetachedFromWindow() {
        this.mDismissByDetachWindow = true;
        if (this.mAnimatorSet != null) {
            this.mAnimatorSet.end();
        }
        super.onDetachedFromWindow();
    }

    public void onRestoreInstanceState(Parcelable state) {
        this.mShowByRestoreState = true;
        super.onRestoreInstanceState(state);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mExpandIcon != null) {
            this.mExpandIcon.draw(canvas);
        }
    }

    int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
        this.mIsContentMeasured = true;
        if (adapter == null) {
            return 0;
        }
        int i = getSelectedItemPosition();
        if (i < 0 || i >= adapter.getCount()) {
            return super.measureContentWidth(adapter, background);
        }
        View itemView = null;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int positionType = adapter.getItemViewType(i);
        if (positionType != 0) {
            int itemType = positionType;
            itemView = null;
        }
        itemView = adapter.getView(i, itemView, this);
        if (itemView instanceof TextView) {
            this.mTextView = (TextView) itemView;
            setTextSize();
        }
        if (itemView.getLayoutParams() == null) {
            itemView.-wrap18(new ViewGroup.LayoutParams(-2, -2));
        }
        itemView.measure(widthMeasureSpec, heightMeasureSpec);
        int width = itemView.getMeasuredWidth();
        this.mTextMinWidth = width;
        if (background != null) {
            background.getPadding(this.mTempRect);
            width += this.mTempRect.left + this.mTempRect.right;
        }
        return width;
    }

    DropdownPopup createDropdownPopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        return new SpinnerDropdownPopup(context, attrs, defStyleAttr, defStyleRes);
    }

    void selectionChanged() {
        if (this.mNeedFireOnSelected) {
            super.selectionChanged();
        }
    }

    public void setDropdownDismissCallback(DropdownDismissCallback callback) {
        this.mDropdownDismissCallback = callback;
    }

    public void setDropdownItemClickListener(OnItemClickListener listener) {
        setOnItemClickListener(listener);
    }

    public void setDropdownUpdateAfterAnim(boolean update) {
        this.mUpdateSelectionAfterAnim = update;
    }

    public boolean isDropDownShowing() {
        return this.mPopup.isShowing();
    }

    private void updateLevel(float value) {
        if (this.mExpandIcon != null) {
            this.mExpandIcon.setLevel((int) (MAX_LEVEL * value));
            invalidate();
        }
    }

    private Animator createIconRotateAnimation(float startValue, final float endValue) {
        if (this.mExpandIcon == null) {
            return null;
        }
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{startValue, endValue});
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorSpinner.this.updateLevel(endValue);
            }
        });
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorSpinner.this.updateLevel(((Float) animation.getAnimatedValue()).floatValue());
            }
        });
        anim.setDuration(ANIM_DURATION);
        anim.setInterpolator(ANIM_INTERPOLATOR_ROTATE);
        return anim;
    }

    private Animator createIconExpandAnimation() {
        return createIconRotateAnimation(0.0f, 1.0f);
    }

    private Animator createIconCollapseAnimation() {
        return createIconRotateAnimation(1.0f, 0.0f);
    }
}

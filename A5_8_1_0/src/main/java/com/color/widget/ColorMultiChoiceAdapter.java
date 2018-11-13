package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ColorListView;
import android.widget.ListView;
import com.color.actionbar.app.ColorActionBarUtil.ActionBarCallback;
import com.color.animation.ColorAnimatorWrapper;
import com.color.animation.ColorAnimatorWrapper.OnSetValuesCallback;
import com.color.animation.ColorAnimatorWrapper.OnSetValuesCallbackAdapter;
import com.color.util.ColorLog;
import java.util.ArrayList;
import java.util.List;

public class ColorMultiChoiceAdapter extends ColorDecoratorAdapter implements ColorActionModeCallback, OnTouchListener {
    /* renamed from: -com-color-widget-ColorMultiChoiceAdapter$AnimatorTypeSwitchesValues */
    private static final /* synthetic */ int[] f1x1adb5657 = null;
    static final int DEFAULT_FINAL_VISIBILITY = -1;
    private static final String TAG_FADE_IN = "FadeIn";
    private static final String TAG_FADE_OUT = "FadeOut";
    private static final String TAG_LEFT_IN = "LeftIn";
    private static final String TAG_LEFT_OUT = "LeftOut";
    private static final String TAG_RIGHT_IN = "RightIn";
    private static final String TAG_RIGHT_OUT = "RightOut";
    private static final String TAG_SPLIT_MENU = "SplitMenu";
    private boolean mActionBarShow;
    private Menu mActionMenu;
    private ActionMode mActionMode;
    private Activity mActivity;
    private AnimatorListener mAlphaListener;
    private List<MenuItem> mBottomMenuItems;
    private List<ColorChoiceModeAnimator> mChoiceAnimators;
    private List<MenuItem> mChoiceMenuItems;
    private List<View> mChoiceViews;
    private View mContentView;
    private List<Animator> mCustomAnimators;
    private long mDurationChoice;
    private List<OnAnimatorsEndListener> mEndListeners;
    private boolean mEnding;
    private boolean mFinishAnimation;
    private boolean mFinishing;
    private boolean mFromLeft;
    private boolean mHasFinishAnimation;
    private boolean mHasStartAnimation;
    private AnimatorListener mHideListener;
    private Interpolator mInterpolatorFadeIn;
    private Interpolator mInterpolatorFadeOut;
    private Interpolator mInterpolatorLeftIn;
    private Interpolator mInterpolatorLeftOut;
    private Interpolator mInterpolatorRightIn;
    private Interpolator mInterpolatorRightOut;
    private AbsListView mListView;
    private AnimatorMaker mMakeFadeIn;
    private AnimatorMaker mMakeFadeOut;
    private AnimatorMaker mMakeLeftIn;
    private AnimatorMaker mMakeLeftOut;
    private AnimatorMaker mMakeRightIn;
    private AnimatorMaker mMakeRightOut;
    private ColorSplitMenuView mMenuView;
    private boolean mRightAnimation;
    private int mRightDependency;
    private OnSetValuesCallback mSetValuesFadeIn;
    private OnSetValuesCallback mSetValuesFadeOut;
    private OnSetValuesCallback mSetValuesLeftIn;
    private OnSetValuesCallback mSetValuesLeftOut;
    private OnSetValuesCallback mSetValuesRightIn;
    private OnSetValuesCallback mSetValuesRightOut;
    private AnimatorListener mShowListener;
    private Menu mSplitMenu;
    private boolean mStartAnimation;
    private boolean mStarting;
    protected final Class<?> mTagClass;
    private AnimatorListener mTranslationXListener;
    private AnimatorListener mTranslationYListener;

    public interface AnimatorMaker {
        ColorChoiceModeAnimator makeAnimator(Context context, View view);
    }

    public enum AnimatorType {
        LEFT_IN,
        LEFT_OUT,
        RIGHT_IN,
        RIGHT_OUT
    }

    public interface ChoiceAnimation {
        void initChoiceView(View view, int i);

        void setChoiceAnimationsRunning(boolean z);
    }

    private class ChoiceAnimatorListener extends AnimatorListenerAdapter {
        private final View mChild;
        private final int mFinalVis;

        public ChoiceAnimatorListener(View child, int finalVis) {
            this.mChild = child;
            this.mFinalVis = finalVis;
        }

        public void onAnimationEnd(Animator animation) {
            ColorMultiChoiceAdapter.this.setViewVisibility(this.mChild, this.mFinalVis);
        }
    }

    public abstract class ChoiceAnimatorMaker implements AnimatorMaker {
        ColorChoiceModeAnimator makeAnimator(List<ColorAnimatorWrapper> list, boolean in, int visibility, int dependency, String tag) {
            return new ColorMultiChoiceAnimator(list, in, visibility, ColorMultiChoiceAdapter.this.mDurationChoice, dependency, tag);
        }
    }

    public interface Choosable2 {
        View findHideCheckView(View view);
    }

    public interface Choosable {
        View findCheckView(View view);

        View findOtherView(View view);

        boolean isChoiceMode();

        void onFinish();

        void setChoiceMode(boolean z);
    }

    public interface ExtraAnimation {
        List<ColorChoiceModeAnimator> getExtraAnimations(ColorMultiChoiceAdapter colorMultiChoiceAdapter, int i, View view, boolean z);

        List<Animator> getExtraAnimators(ColorMultiChoiceAdapter colorMultiChoiceAdapter, int i, View view, boolean z);

        void onBindView(int i, View view);
    }

    public class FadeAnimatorMaker extends ChoiceAnimatorMaker {
        boolean mIn = false;

        FadeAnimatorMaker(boolean in) {
            super();
            this.mIn = in;
        }

        public ColorChoiceModeAnimator makeAnimator(Context context, View target) {
            List<ColorAnimatorWrapper> list = new ArrayList();
            String tag = addAnimation(list, target);
            return makeAnimator(list, this.mIn, -1, getRightDependency(), ColorMultiChoiceAdapter.this.makeTag(target, tag));
        }

        String addAnimation(List<ColorAnimatorWrapper> list, View target) {
            list.add(new ColorAnimatorWrapper(ColorMultiChoiceAdapter.this.createFadeAnimator(this.mIn, target, ColorMultiChoiceAdapter.this.mAlphaListener), this.mIn ? ColorMultiChoiceAdapter.this.mSetValuesFadeIn : ColorMultiChoiceAdapter.this.mSetValuesFadeOut));
            if (this.mIn) {
                return ColorMultiChoiceAdapter.TAG_FADE_IN;
            }
            return ColorMultiChoiceAdapter.TAG_FADE_OUT;
        }

        private int getRightDependency() {
            return ColorMultiChoiceAdapter.this.isActionBarShow() ? 0 : ColorMultiChoiceAdapter.this.mRightDependency;
        }
    }

    private class LeftAnimatorMaker extends FadeAnimatorMaker {
        LeftAnimatorMaker(boolean in) {
            super(in);
        }

        String addAnimation(List<ColorAnimatorWrapper> list, View target) {
            super.addAnimation(list, target);
            list.add(new ColorAnimatorWrapper(ColorMultiChoiceAdapter.this.createLeftAnimator(this.mIn, target, ColorMultiChoiceAdapter.this.mTranslationXListener), this.mIn ? ColorMultiChoiceAdapter.this.mSetValuesLeftIn : ColorMultiChoiceAdapter.this.mSetValuesLeftOut));
            if (this.mIn) {
                return ColorMultiChoiceAdapter.TAG_LEFT_IN;
            }
            return ColorMultiChoiceAdapter.TAG_LEFT_OUT;
        }
    }

    public interface OnAnimatorsEndListener {
        void onAnimatorsEnd(boolean z);
    }

    private class RightAnimatorMaker extends FadeAnimatorMaker {
        RightAnimatorMaker(boolean in) {
            super(in);
        }

        String addAnimation(List<ColorAnimatorWrapper> list, View target) {
            super.addAnimation(list, target);
            list.add(new ColorAnimatorWrapper(ColorMultiChoiceAdapter.this.createRightAnimator(this.mIn, target, ColorMultiChoiceAdapter.this.mTranslationXListener), this.mIn ? ColorMultiChoiceAdapter.this.mSetValuesRightIn : ColorMultiChoiceAdapter.this.mSetValuesRightOut));
            if (this.mIn) {
                return ColorMultiChoiceAdapter.TAG_RIGHT_IN;
            }
            return ColorMultiChoiceAdapter.TAG_RIGHT_OUT;
        }
    }

    private class SplitMenuAnimator extends ColorChoiceModeAnimator {
        public SplitMenuAnimator(ColorMultiChoiceAdapter this$0, List<ColorAnimatorWrapper> list, String tag) {
            this(list, tag, 0);
        }

        public SplitMenuAnimator(List<ColorAnimatorWrapper> list, String tag, int dependency) {
            super(list, tag, dependency);
        }

        public void initialize() {
            for (ColorAnimatorWrapper animation : this.mAnimWrapperList) {
                animation.getAnimation().addListener(this);
            }
        }

        public void addListener(AnimatorListener listener) {
            if (!this.mAnimWrapperList.isEmpty()) {
                ((ColorAnimatorWrapper) this.mAnimWrapperList.get(0)).getAnimation().addListener(listener);
            }
        }
    }

    /* renamed from: -getcom-color-widget-ColorMultiChoiceAdapter$AnimatorTypeSwitchesValues */
    private static /* synthetic */ int[] m1x837f54fb() {
        if (f1x1adb5657 != null) {
            return f1x1adb5657;
        }
        int[] iArr = new int[AnimatorType.values().length];
        try {
            iArr[AnimatorType.LEFT_IN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AnimatorType.LEFT_OUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AnimatorType.RIGHT_IN.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AnimatorType.RIGHT_OUT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f1x1adb5657 = iArr;
        return iArr;
    }

    public ColorMultiChoiceAdapter(BaseAdapter adapter, Activity activity, AbsListView listView, ColorSplitMenuView menuView) {
        super(adapter);
        this.mTagClass = getClass();
        this.mRightDependency = 0;
        this.mDurationChoice = 0;
        this.mActionBarShow = false;
        this.mStarting = false;
        this.mFinishing = false;
        this.mEnding = false;
        this.mRightAnimation = false;
        this.mHasStartAnimation = true;
        this.mHasFinishAnimation = false;
        this.mStartAnimation = false;
        this.mFinishAnimation = false;
        this.mInterpolatorLeftIn = null;
        this.mInterpolatorLeftOut = null;
        this.mInterpolatorRightIn = null;
        this.mInterpolatorRightOut = null;
        this.mInterpolatorFadeIn = null;
        this.mInterpolatorFadeOut = null;
        this.mActivity = null;
        this.mListView = null;
        this.mContentView = null;
        this.mSplitMenu = null;
        this.mActionMenu = null;
        this.mActionMode = null;
        this.mChoiceMenuItems = new ArrayList();
        this.mBottomMenuItems = new ArrayList();
        this.mChoiceViews = new ArrayList();
        this.mEndListeners = new ArrayList();
        this.mChoiceAnimators = new ArrayList();
        this.mCustomAnimators = new ArrayList();
        this.mMakeLeftIn = new LeftAnimatorMaker(true);
        this.mMakeLeftOut = new LeftAnimatorMaker(false);
        this.mMakeRightIn = new RightAnimatorMaker(true);
        this.mMakeRightOut = new RightAnimatorMaker(false);
        this.mMakeFadeIn = new FadeAnimatorMaker(true);
        this.mMakeFadeOut = new FadeAnimatorMaker(false);
        this.mMenuView = null;
        this.mShowListener = new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (!ColorMultiChoiceAdapter.this.mEnding) {
                    ColorLog.d("log.key.multi_choice.anim", ColorMultiChoiceAdapter.this.mTagClass, new Object[]{"onAnimationStart : ActionBar.Show"});
                }
            }

            public void onAnimationEnd(Animator animation) {
                if (!ColorMultiChoiceAdapter.this.mEnding) {
                    ColorLog.d("log.key.multi_choice.anim", ColorMultiChoiceAdapter.this.mTagClass, new Object[]{"onAnimationEnd : ActionBar.Show"});
                    ColorMultiChoiceAdapter.this.mEnding = true;
                    ColorMultiChoiceAdapter.this.onAllAnimatorsEnd(true);
                }
            }
        };
        this.mHideListener = new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (!ColorMultiChoiceAdapter.this.mEnding) {
                    ColorLog.d("log.key.multi_choice.anim", ColorMultiChoiceAdapter.this.mTagClass, new Object[]{"onAnimationStart : ActionBar.Hide"});
                }
            }

            public void onAnimationEnd(Animator animation) {
                if (!ColorMultiChoiceAdapter.this.mEnding) {
                    ColorLog.d("log.key.multi_choice.anim", ColorMultiChoiceAdapter.this.mTagClass, new Object[]{"onAnimationEnd : ActionBar.Hide"});
                    ColorMultiChoiceAdapter.this.mEnding = true;
                    ColorMultiChoiceAdapter.this.onHideAnimatorsEnd();
                    ColorMultiChoiceAdapter.this.onAllAnimatorsEnd(false);
                }
            }
        };
        this.mSetValuesLeftIn = new OnSetValuesCallbackAdapter() {
            public void initialize(View target) {
                target.setTranslationX(getStartValue(target));
            }

            public float getStartValue(View target) {
                return (float) (-ColorMultiChoiceAdapter.this.getTargetWidth(target));
            }
        };
        this.mSetValuesLeftOut = new OnSetValuesCallbackAdapter() {
            public void initialize(View target) {
                target.setTranslationX(getStartValue(target));
            }

            public float getEndValue(View target) {
                return (float) (-ColorMultiChoiceAdapter.this.getTargetWidth(target));
            }
        };
        this.mSetValuesRightIn = new OnSetValuesCallbackAdapter() {
            public void initialize(View target) {
                target.setTranslationX(getStartValue(target));
            }

            public float getStartValue(View target) {
                return (float) ColorMultiChoiceAdapter.this.getTargetWidth(target);
            }
        };
        this.mSetValuesRightOut = new OnSetValuesCallbackAdapter() {
            public void initialize(View target) {
                target.setTranslationX(getStartValue(target));
            }

            public float getEndValue(View target) {
                return (float) ColorMultiChoiceAdapter.this.getTargetWidth(target);
            }
        };
        this.mSetValuesFadeIn = new OnSetValuesCallbackAdapter() {
            public void initialize(View target) {
                target.setAlpha(getStartValue(target));
            }

            public float getEndValue(View target) {
                return 1.0f;
            }
        };
        this.mSetValuesFadeOut = new OnSetValuesCallbackAdapter() {
            public void initialize(View target) {
                target.setAlpha(getStartValue(target));
            }

            public float getStartValue(View target) {
                return 1.0f;
            }
        };
        this.mTranslationXListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (animation instanceof ObjectAnimator) {
                    Object target = ((ObjectAnimator) animation).getTarget();
                    if (target instanceof View) {
                        ((View) target).setTranslationX(0.0f);
                    }
                }
            }
        };
        this.mTranslationYListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (animation instanceof ObjectAnimator) {
                    Object target = ((ObjectAnimator) animation).getTarget();
                    if (target instanceof View) {
                        ((View) target).setTranslationY(0.0f);
                    }
                }
            }
        };
        this.mAlphaListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (animation instanceof ObjectAnimator) {
                    Object target = ((ObjectAnimator) animation).getTarget();
                    if (target instanceof View) {
                        ((View) target).setAlpha(1.0f);
                    }
                }
            }
        };
        this.mFromLeft = false;
        this.mActivity = initActivity(activity);
        this.mListView = initListView(listView);
        this.mMenuView = initSplitMenu(menuView);
        initFromResources();
        setActionBarShow(getActionBarShow());
        initActionBar();
    }

    public ColorMultiChoiceAdapter(BaseAdapter adapter, Activity activity, AbsListView listView) {
        this(adapter, activity, listView, null);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        if (needSetRightView(convertView)) {
            setCheckView(convertView, position);
            setOtherView(convertView, position);
            if (this.mAdapter instanceof ExtraAnimation) {
                ((ExtraAnimation) this.mAdapter).onBindView(position, convertView);
            }
        }
        return convertView;
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mActionMode = mode;
        this.mActionMenu = menu;
        this.mStarting = true;
        setChoiceMode(true);
        if (this.mAdapter instanceof ChoiceAnimation) {
            ((ChoiceAnimation) this.mAdapter).setChoiceAnimationsRunning(true);
        }
        addChoiceAnimators(true);
        return true;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    public void onDestroyActionMode(ActionMode mode) {
        ColorLog.d("log.key.multi_choice.disp", this.mTagClass, new Object[]{"onDestroyActionMode"});
        this.mActionMode = null;
        this.mActionMenu = null;
        this.mSplitMenu = null;
        this.mFinishing = true;
        setChoiceMode(false);
        this.mChoiceAnimators.clear();
        this.mCustomAnimators.clear();
        if (this.mAdapter instanceof ChoiceAnimation) {
            ((ChoiceAnimation) this.mAdapter).setChoiceAnimationsRunning(true);
        }
        if (needChoiceAnimators()) {
            addChoiceAnimators(false);
        }
        prepareBottomMenu();
        notifyDataSetChanged();
    }

    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        notifyDataSetChanged();
    }

    public boolean onCreateSplitMenu(ActionMode mode, Menu menu) {
        this.mSplitMenu = menu;
        return true;
    }

    public boolean onPrepareSplitMenu(ActionMode mode, Menu menu) {
        return false;
    }

    public boolean onSplitItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    public void onStartActionMode(ActionMode mode) {
        if (!(this.mMenuView == null || this.mSplitMenu == null)) {
            this.mMenuView.bindMenu(this.mSplitMenu);
        }
        prepareChoiceMenu();
        notifyDataSetChanged();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return isAnimationsRunning();
    }

    void setAdapter(BaseAdapter adapter) {
        if (adapter instanceof Choosable) {
            super.setAdapter(adapter);
            return;
        }
        throw new RuntimeException("Your adapter must implement interface " + getClass().getName() + ".Choosable");
    }

    public void addOnAnimatorsEndListener(OnAnimatorsEndListener listener) {
        this.mEndListeners.add(listener);
    }

    public void removeOnAnimatorsEndListener(OnAnimatorsEndListener listener) {
        this.mEndListeners.remove(listener);
    }

    public void setOtherAnimation(boolean right) {
        this.mRightAnimation = right;
    }

    public void setRightDependency(int dependency) {
        this.mRightDependency = dependency;
    }

    public ActionMode getActionMode() {
        return this.mActionMode;
    }

    public Menu getActionMenu() {
        return this.mActionMenu;
    }

    public Menu getSplitMenu() {
        return this.mSplitMenu;
    }

    public AbsListView getListView() {
        return this.mListView;
    }

    public void startActionMode() {
        this.mChoiceAnimators.clear();
        this.mCustomAnimators.clear();
        setNotifyCheckedStateEnabled(false);
        this.mListView.setItemChecked(0, true);
        this.mListView.clearChoices();
        setNotifyCheckedStateEnabled(true);
        this.mAdapter.notifyDataSetChanged();
    }

    public boolean isChoiceMode() {
        return ((Choosable) this.mAdapter).isChoiceMode();
    }

    public boolean isAnimationsRunning() {
        return !this.mStarting ? this.mFinishing : true;
    }

    public boolean isActionBarShow() {
        return this.mActionBarShow;
    }

    public void setActionBarShow(boolean isShow) {
        this.mActionBarShow = isShow;
    }

    public void lockMenuUpdate() {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).lockMenuUpdate();
        }
        if (this.mMenuView != null) {
            this.mMenuView.lockMenuUpdate();
        }
    }

    public void unlockMenuUpdate() {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).unlockMenuUpdate();
        }
        if (this.mMenuView != null) {
            this.mMenuView.unlockMenuUpdate();
        }
    }

    public AnimatorMaker getAnimatorMaker(AnimatorType type) {
        switch (m1x837f54fb()[type.ordinal()]) {
            case 1:
                return this.mMakeLeftIn;
            case 2:
                return this.mMakeLeftOut;
            case 3:
                return this.mMakeRightIn;
            case 4:
                return this.mMakeRightOut;
            default:
                return null;
        }
    }

    public ColorChoiceModeAnimator makeChoiceAnimator(int position, View target, AnimatorMaker maker, AnimatorListener listener) {
        ColorChoiceModeAnimator animation = null;
        if (target != null) {
            animation = maker.makeAnimator(this.mActivity, target);
            animation.appendTag("." + position);
            if (listener != null) {
                animation.addListener(listener);
            }
        }
        return animation;
    }

    public void setHasStartAnimation(boolean hasAnim) {
        this.mHasStartAnimation = hasAnim;
    }

    public void setHasFinishAnimation(boolean hasAnim) {
        this.mHasFinishAnimation = hasAnim;
    }

    void setActionModeCallaback(ColorActionModeCallback callback) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setActionModeCallback(callback);
        }
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private boolean needSetRightView(View view) {
        if (view == null || ((this.mStartAnimation || (this.mFinishAnimation ^ 1) == 0) && isAnimationsRunning())) {
            return false;
        }
        return true;
    }

    private View findHideCheckView(View parent) {
        if (this.mAdapter instanceof Choosable2) {
            return ((Choosable2) this.mAdapter).findHideCheckView(parent);
        }
        return null;
    }

    private void setCheckView(View parent, int position) {
        setViewVisibility(((Choosable) this.mAdapter).findCheckView(parent), isChoiceMode() ? 0 : 8);
        View hideCheckView = findHideCheckView(parent);
        if (hideCheckView != null) {
            setViewVisibility(hideCheckView, isChoiceMode() ? 4 : 8);
        }
    }

    private void setOtherView(View parent, int position) {
        setViewVisibility(((Choosable) this.mAdapter).findOtherView(parent), isChoiceMode() ? 8 : 0);
    }

    private void prepareMenu(List<MenuItem> menuItems) {
        if (this.mMenuView != null) {
            this.mMenuView.setMenuUpdateMode(1);
            List<ColorAnimatorWrapper> animList = new ArrayList();
            animList.add(new ColorAnimatorWrapper(this.mMenuView.getUpdater(menuItems, true), null));
            addAnimator(new SplitMenuAnimator(this, animList, makeTag(this.mMenuView, TAG_SPLIT_MENU)));
            this.mMenuView.setMenuUpdateMode(0);
        }
    }

    private void prepareBottomMenu() {
        prepareMenu(this.mBottomMenuItems);
    }

    private void prepareChoiceMenu() {
        if (this.mSplitMenu != null) {
            this.mChoiceMenuItems.clear();
            for (int i = 0; i < this.mSplitMenu.size(); i++) {
                this.mChoiceMenuItems.add(this.mSplitMenu.getItem(i));
            }
        }
        prepareMenu(this.mChoiceMenuItems);
    }

    private boolean needChoiceAnimators() {
        return isActionBarShow();
    }

    private void setChoiceMode(boolean choice) {
        ((Choosable) this.mAdapter).setChoiceMode(choice);
    }

    private void finishActionMode(List<Integer> positions) {
        if (this.mActionMode != null) {
            onFinishActionMode(positions);
            this.mActionMode.finish();
        }
    }

    private void onFinishActionMode(List<Integer> list) {
    }

    private void onHideAnimatorsEnd() {
        ((Choosable) this.mAdapter).onFinish();
    }

    private void onAllAnimatorsEnd(boolean start) {
        this.mChoiceAnimators.clear();
        this.mCustomAnimators.clear();
        this.mEnding = false;
        this.mStartAnimation = false;
        this.mFinishAnimation = false;
        if (this.mAdapter instanceof ChoiceAnimation) {
            ((ChoiceAnimation) this.mAdapter).setChoiceAnimationsRunning(false);
        }
        notifyAnimatorsEnd(start);
        notifyDataSetChanged();
        this.mStarting = false;
        this.mFinishing = false;
    }

    private void addAnimator(ColorChoiceModeAnimator animation) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (animation != null && (actionBar instanceof ActionBarCallback)) {
            ColorLog.d("log.key.multi_choice.anim", this.mTagClass, new Object[]{"addAnimator : " + animation.getTag()});
            animation.initialize();
            if (animation.getDependency() == 1) {
                ((ActionBarCallback) actionBar).addAfterAnimatorWrappers(animation.getAnimations());
            } else if (animation.getDependency() == 2) {
                ((ActionBarCallback) actionBar).addBeforeAnimatorWrappers(animation.getAnimations());
            } else {
                ((ActionBarCallback) actionBar).addWithAnimatorWrappers(animation.getAnimations());
            }
        }
    }

    private void addAnimators(List<ColorChoiceModeAnimator> animations) {
        for (ColorChoiceModeAnimator animation : animations) {
            addAnimator(animation);
        }
    }

    private void addCustomAnimator(Animator animator) {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (animator != null && (actionBar instanceof ActionBarCallback)) {
            ((ActionBarCallback) actionBar).addWithAnimator(animator);
        }
    }

    private void addCustomAnimators(List<Animator> animators) {
        for (Animator animator : animators) {
            addCustomAnimator(animator);
        }
    }

    private String makeTag(View view, boolean forceMenuBar, String tag) {
        if (view != null) {
            return view.getClass().getSimpleName() + "." + tag;
        }
        return tag;
    }

    private String makeTag(View view, String tag) {
        return makeTag(view, false, tag);
    }

    private int getPositionForView(View view) {
        int headers = 0;
        if (this.mListView instanceof ListView) {
            headers = ((ListView) this.mListView).getHeaderViewsCount();
        }
        return this.mListView.getPositionForView(view) - headers;
    }

    private void makeChoiceAnimators(AnimatorMaker checkMaker, boolean choice) {
        for (int i = 0; i < this.mListView.getChildCount(); i++) {
            View child = this.mListView.getChildAt(i);
            int position = getPositionForView(child);
            if (position >= 0) {
                this.mChoiceViews.add(child);
                if (this.mAdapter instanceof ChoiceAnimation) {
                    ((ChoiceAnimation) this.mAdapter).initChoiceView(child, position);
                }
                View checkView = ((Choosable) this.mAdapter).findCheckView(child);
                View otherView = ((Choosable) this.mAdapter).findOtherView(child);
                if (this.mStarting) {
                    if (this.mHasStartAnimation) {
                        this.mStartAnimation = true;
                        createChoiceAnimators(this.mChoiceAnimators, position, checkView, checkMaker, new ChoiceAnimatorListener(otherView, 8));
                    }
                } else if (this.mFinishing && this.mHasFinishAnimation) {
                    this.mFinishAnimation = true;
                    createChoiceAnimators(this.mChoiceAnimators, position, checkView, checkMaker, new ChoiceAnimatorListener(otherView, 0));
                }
                if (this.mAdapter instanceof ExtraAnimation) {
                    List<ColorChoiceModeAnimator> animChoiceList = ((ExtraAnimation) this.mAdapter).getExtraAnimations(this, position, child, choice);
                    if (animChoiceList != null) {
                        this.mChoiceAnimators.addAll(animChoiceList);
                    }
                    List<Animator> animList = ((ExtraAnimation) this.mAdapter).getExtraAnimators(this, position, child, choice);
                    if (animList != null) {
                        this.mCustomAnimators.addAll(animList);
                    }
                }
            }
        }
    }

    private AnimatorMaker getCheckAnimator(boolean choice) {
        AnimatorMaker makerIn = this.mFromLeft ? this.mMakeLeftIn : this.mMakeRightIn;
        AnimatorMaker makerOut = this.mFromLeft ? this.mMakeLeftOut : this.mMakeRightOut;
        if (choice) {
            return makerIn;
        }
        return makerOut;
    }

    public void setFromLeft(boolean from) {
        this.mFromLeft = from;
    }

    private int getTargetWidth(View target) {
        if (target == null) {
            return 0;
        }
        int width = target.getWidth();
        if (width != 0) {
            return width;
        }
        measureTarget(target);
        return target.getMeasuredWidth();
    }

    private int getDefaultMeasureSpec() {
        return MeasureSpec.makeMeasureSpec(0, 0);
    }

    private void measureTarget(View target) {
        target.measure(getDefaultMeasureSpec(), getDefaultMeasureSpec());
    }

    private void addChoiceAnimators(boolean choice) {
        makeChoiceAnimators(getCheckAnimator(choice), choice);
        addAnimators(this.mChoiceAnimators);
        addCustomAnimators(this.mCustomAnimators);
    }

    private void createChoiceAnimators(List<ColorChoiceModeAnimator> animations, int position, View target, AnimatorMaker maker, AnimatorListener listener) {
        ColorChoiceModeAnimator animation = makeChoiceAnimator(position, target, maker, listener);
        if (animation != null) {
            animations.add(animation);
        }
    }

    private boolean getActionBarShow() {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            return actionBar.isShowing();
        }
        return false;
    }

    private void initActionBar() {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).addShowListener(this.mShowListener);
            ((ActionBarCallback) actionBar).addHideListener(this.mHideListener);
        }
    }

    private ValueAnimator createLeftAnimator(boolean in, View target, AnimatorListener listener) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(target, "translationX", new float[]{0.0f, 0.0f});
        if (listener != null) {
            anim.addListener(listener);
        }
        return anim;
    }

    private ValueAnimator createRightAnimator(boolean in, View target, AnimatorListener listener) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(target, "translationX", new float[]{0.0f, 0.0f});
        anim.setInterpolator(in ? this.mInterpolatorRightIn : this.mInterpolatorRightOut);
        if (listener != null) {
            anim.addListener(listener);
        }
        return anim;
    }

    private ValueAnimator createFadeAnimator(boolean in, View target, AnimatorListener listener) {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(target, "alpha", new float[]{0.0f, 0.0f});
        fadeAnim.setInterpolator(in ? this.mInterpolatorFadeIn : this.mInterpolatorFadeOut);
        if (listener != null) {
            fadeAnim.addListener(listener);
        }
        return fadeAnim;
    }

    private void initFromResources() {
        this.mDurationChoice = (long) this.mActivity.getResources().getInteger(202179602);
        this.mInterpolatorLeftIn = AnimationUtils.loadInterpolator(this.mActivity, 202309634);
        this.mInterpolatorLeftOut = AnimationUtils.loadInterpolator(this.mActivity, 202309635);
        this.mInterpolatorRightIn = AnimationUtils.loadInterpolator(this.mActivity, 202309632);
        this.mInterpolatorRightOut = AnimationUtils.loadInterpolator(this.mActivity, 202309633);
        this.mInterpolatorFadeIn = AnimationUtils.loadInterpolator(this.mActivity, 202310662);
        this.mInterpolatorFadeOut = AnimationUtils.loadInterpolator(this.mActivity, 202310663);
    }

    private Activity initActivity(Activity activity) {
        if (activity != null) {
            return activity;
        }
        throw new RuntimeException("Your activity is null");
    }

    private AbsListView initListView(AbsListView listView) {
        if (listView == null) {
            throw new RuntimeException("Your listview to make animation is null");
        }
        listView.setAdapter(this);
        listView.setOnTouchListener(this);
        return listView;
    }

    private ColorSplitMenuView initSplitMenu(ColorSplitMenuView menuView) {
        if (menuView != null) {
            this.mBottomMenuItems.addAll(menuView.getMenuItems());
        }
        return menuView;
    }

    private void notifyAnimatorsEnd(boolean start) {
        this.mChoiceViews.clear();
        for (OnAnimatorsEndListener listener : (List) ((ArrayList) this.mEndListeners).clone()) {
            listener.onAnimatorsEnd(start);
        }
    }

    private void setNotifyCheckedStateEnabled(boolean enable) {
        if (this.mListView instanceof ColorListView) {
            ((ColorListView) this.mListView).setNotifyCheckedStateEnabled(enable);
        }
    }
}

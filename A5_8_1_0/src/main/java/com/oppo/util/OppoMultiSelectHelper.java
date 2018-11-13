package com.oppo.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import com.color.actionbar.app.ColorActionBarUtil.ActionBarCallback;
import com.color.util.ColorLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class OppoMultiSelectHelper implements Callback, AnimationListener, ActivityLifecycleCallbacks {
    private static final long DEFAULT_DURATION_OFFSET = 0;
    private static final int DEFAULT_FINAL_VISIBILITY = -1;
    private static final int DEFAULT_FLOW_LAYOUT = 201917494;
    private static final String TAG_BOTTOM_IN = "BottomIn";
    private static final String TAG_BOTTOM_OUT = "BottomOut";
    private static final String TAG_FADE_IN = "FadeIn";
    private static final String TAG_FADE_OUT = "FadeOut";
    private static final String TAG_RIGHT_IN = "RightIn";
    private static final String TAG_RIGHT_OUT = "RightOut";
    private static List<OppoAnimationHelper> mBottomInList = new ArrayList();
    private static List<OppoAnimationHelper> mBottomOutList = new ArrayList();
    private static List<OppoAnimationHelper> mFadeInList = new ArrayList();
    private static List<OppoAnimationHelper> mFadeOutList = new ArrayList();
    private static List<OppoAnimationHelper> mLeftInList = new ArrayList();
    private static List<OppoAnimationHelper> mLeftOutList = new ArrayList();
    private static List<OppoAnimationHelper> mRightInList = new ArrayList();
    private static List<OppoAnimationHelper> mRightOutList = new ArrayList();
    private boolean mActionBarAnimating;
    private boolean mActionBarShow;
    private ActionMode mActionMode;
    private Activity mActivity;
    private View mBottomExtra;
    private OppoAnimationHelper mBottomIn;
    private View mBottomMenu;
    private OppoAnimationHelper mBottomOut;
    private Callback mCallback;
    private boolean mClearing;
    private OppoAnimationHelper mExtraIn;
    private OppoAnimationHelper mExtraOut;
    private boolean mFinishing;
    private OppoAnimationHelper mFlowIn;
    private View mFlowMenu;
    private OppoAnimationHelper mFlowOut;
    private OnAnimationsEndListener mListener;
    private int mMajorVisibility;
    private List<OppoAnimationHelper> mRunningList;
    private boolean mStarting;
    protected final Class<?> mTagClass;

    public interface OnAnimationsEndListener {
        void onAnimationsEnd();
    }

    public OppoMultiSelectHelper(Activity activity, View view) {
        this(activity, view, (int) DEFAULT_FLOW_LAYOUT);
    }

    public OppoMultiSelectHelper(Activity activity, View view, int layout) {
        this(activity, view, layout, DEFAULT_DURATION_OFFSET);
    }

    public OppoMultiSelectHelper(Activity activity, View view, long offset) {
        this(activity, view, DEFAULT_FLOW_LAYOUT, offset);
    }

    public OppoMultiSelectHelper(Activity activity, View view, int layout, long offset) {
        this.mTagClass = getClass();
        this.mActionBarShow = false;
        this.mActionBarAnimating = false;
        this.mClearing = false;
        this.mStarting = false;
        this.mFinishing = false;
        this.mMajorVisibility = 0;
        this.mCallback = null;
        this.mActionMode = null;
        this.mActivity = null;
        this.mBottomMenu = null;
        this.mBottomExtra = null;
        this.mFlowMenu = null;
        this.mBottomIn = null;
        this.mBottomOut = null;
        this.mExtraIn = null;
        this.mExtraOut = null;
        this.mFlowIn = null;
        this.mFlowOut = null;
        this.mListener = null;
        this.mRunningList = new ArrayList();
        this.mActivity = initActivity(activity);
        this.mBottomMenu = initBottomMenu(view);
        this.mFlowMenu = createFlowMenu(layout);
        this.mBottomIn = makeBottomIn(this.mActivity, this.mBottomMenu, getBottomInVisibility(), offset);
        this.mBottomOut = makeBottomOut(this.mActivity, this.mBottomMenu, getBottomOutVisibility(), offset);
        this.mFlowIn = makeBottomIn(this.mActivity, this.mFlowMenu, getFlowInVisibility(), true, offset);
        this.mFlowOut = makeBottomOut(this.mActivity, this.mFlowMenu, getFlowOutVisibility(), true, offset);
        setActionBarShow(getActionBarShow());
        initActionBar();
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        if (isAnimationMatch(animation)) {
            tryClearAnimations();
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (this.mCallback != null) {
            return this.mCallback.onCreateActionMode(mode, menu);
        }
        return false;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (this.mCallback != null) {
            return this.mCallback.onPrepareActionMode(mode, menu);
        }
        return false;
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (this.mCallback != null) {
            return this.mCallback.onActionItemClicked(mode, item);
        }
        return false;
    }

    public void onDestroyActionMode(ActionMode mode) {
        ColorLog.e("log.key.multi_select.disp", this.mTagClass, new Object[]{"onDestroyActionMode"});
        doClearAnimations();
        this.mFinishing = true;
        updateMenuBar(this.mFlowMenu, false);
        showMajorGroup(this.mBottomMenu, false);
        if (this.mCallback != null) {
            this.mCallback.onDestroyActionMode(mode);
        }
        if (this.mBottomMenu != null) {
            if (needBottomAnimation()) {
                if (this.mMajorVisibility == 0) {
                    startBottomAnimation(this.mBottomIn);
                }
                startBottomAnimation(this.mExtraIn);
            } else {
                showBottomExtra(this.mBottomExtra, true);
            }
            startBottomAnimation(this.mFlowOut);
        }
        this.mActionMode = null;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
    }

    public void onActivityPaused(Activity activity) {
        if (this.mActivity == activity) {
            doClearAnimations();
        }
    }

    public void onActivityStopped(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
        if (this.mActivity == activity) {
            recycle();
            this.mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
            this.mActivity = null;
        }
    }

    public boolean isActionBarShow() {
        return this.mActionBarShow;
    }

    public void setActionBarShow(boolean isShow) {
        this.mActionBarShow = isShow;
    }

    public void startActionMode(Callback callback) {
        this.mCallback = callback;
        getBottomInfo();
        updateMenuBar(this.mFlowMenu, false);
        onStartActionMode();
    }

    public void finishActionMode() {
        onFinishActionMode();
    }

    public ActionMode getActionMode() {
        return this.mActionMode;
    }

    public void setExtraBottomView(View view) {
        if (view == null) {
            throw new RuntimeException("Your extra view to make animation is null");
        }
        this.mBottomExtra = view;
        this.mExtraIn = makeBottomIn(this.mActivity, view, -1, DEFAULT_DURATION_OFFSET);
        this.mExtraOut = makeBottomOut(this.mActivity, view, -1, DEFAULT_DURATION_OFFSET);
    }

    public void setOnAnimationsEndListener(OnAnimationsEndListener listener) {
        this.mListener = listener;
    }

    public void startRightAnimation(OppoAnimationHelper helper) {
        if (helper == null) {
            return;
        }
        if (isAnimationsRunning()) {
            ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"startRightAnimation : ", helper.getTag(), "=", helper.getView()});
            this.mRunningList.add(helper);
            helper.start(false, this);
            return;
        }
        ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"endRightAnimation : ", helper.getTag(), "=", helper.getView()});
        helper.end();
    }

    public void startLeftAnimation(OppoAnimationHelper helper) {
        if (helper == null) {
            return;
        }
        if (isAnimationsRunning()) {
            ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"startLeftAnimation : ", helper.getTag(), "=", helper.getView()});
            this.mRunningList.add(helper);
            helper.start(false, this);
            return;
        }
        ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"endLeftAnimation : ", helper.getTag(), "=", helper.getView()});
        helper.end();
    }

    public boolean isAnimationsRunning() {
        return !this.mStarting ? this.mFinishing : true;
    }

    public void doClearAnimations() {
        if (!this.mClearing) {
            this.mClearing = true;
            clearAnimations();
            if (this.mStarting) {
                showBottomExtra(this.mBottomExtra, false);
                showMajorGroup(this.mBottomMenu, true);
                updateMenuBar(this.mBottomMenu, true);
            }
            this.mStarting = false;
            this.mFinishing = false;
            if (this.mListener != null) {
                this.mListener.onAnimationsEnd();
            }
            this.mClearing = false;
        }
    }

    public void recycle() {
        OppoAnimationHelper helper;
        Iterator it = mBottomInList.iterator();
        while (it.hasNext()) {
            helper = (OppoAnimationHelper) it.next();
            if (helper == this.mBottomIn || helper == this.mExtraIn || helper == this.mFlowIn) {
                it.remove();
            }
        }
        it = mBottomOutList.iterator();
        while (it.hasNext()) {
            helper = (OppoAnimationHelper) it.next();
            if (helper == this.mBottomOut || helper == this.mExtraOut || helper == this.mFlowOut) {
                it.remove();
            }
        }
        mRightInList.clear();
        mRightOutList.clear();
        mFadeInList.clear();
        mFadeOutList.clear();
        this.mRunningList.clear();
        mLeftInList.clear();
        mLeftOutList.clear();
        ColorLog.e("log.key.multi_select.disp", this.mTagClass, new Object[]{"recycle : mBottomInList=", Integer.valueOf(mBottomInList.size()), ", mBottomOutList=", Integer.valueOf(mBottomOutList.size())});
    }

    public void recycleAnimation(OppoAnimationHelper helper) {
        mBottomInList.remove(helper);
        mBottomOutList.remove(helper);
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, long offset) {
        return makeBottomIn(context, view, visibility, false, offset, makeTag(view, false, TAG_BOTTOM_IN));
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view, long offset) {
        return makeBottomIn(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility) {
        return makeBottomIn(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view) {
        return makeBottomIn(context, view, -1);
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, long offset) {
        return makeBottomOut(context, view, visibility, false, offset, makeTag(view, false, TAG_BOTTOM_OUT));
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view, long offset) {
        return makeBottomOut(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility) {
        return makeBottomOut(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view) {
        return makeBottomOut(context, view, -1);
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view, int visibility, long offset) {
        return makeLeftIn(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_IN));
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view, long offset) {
        return makeLeftIn(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view, int visibility) {
        return makeLeftIn(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view) {
        return makeLeftIn(context, view, -1);
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view, int visibility, long offset) {
        return makeLeftOut(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_OUT));
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view, long offset) {
        return makeLeftOut(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view, int visibility) {
        return makeLeftOut(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view) {
        return makeLeftOut(context, view, -1);
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view, int visibility, long offset) {
        return makeRightIn(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_IN));
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view, long offset) {
        return makeRightIn(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view, int visibility) {
        return makeRightIn(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view) {
        return makeRightIn(context, view, -1);
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view, int visibility, long offset) {
        return makeRightOut(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_OUT));
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view, long offset) {
        return makeRightOut(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view, int visibility) {
        return makeRightOut(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view) {
        return makeRightOut(context, view, -1);
    }

    public static OppoAnimationHelper makeFadeIn(Context context, View view) {
        return makeFadeIn(context, view, -1, true, DEFAULT_DURATION_OFFSET, makeTag(view, false, TAG_FADE_IN));
    }

    public static OppoAnimationHelper makeFadeOut(Context context, View view) {
        return makeFadeOut(context, view, -1, false, DEFAULT_DURATION_OFFSET, makeTag(view, false, TAG_FADE_OUT));
    }

    public static OppoAnimationHelper makeItemUp(Context context, View view) {
        return null;
    }

    public static OppoAnimationHelper makeItemLeft(Context context, View view) {
        return null;
    }

    private static View getBottomView(View view, boolean forceMenuBar) {
        return view;
    }

    private static String makeTag(View view, boolean forceMenuBar, String tag) {
        if (view == null) {
            return tag;
        }
        return getBottomView(view, forceMenuBar).getClass().getSimpleName() + "." + tag;
    }

    private static OppoAnimationHelper findHelper(View view, List<OppoAnimationHelper> helpers) {
        for (OppoAnimationHelper helper : helpers) {
            if (helper.getView() == view) {
                return helper;
            }
        }
        return null;
    }

    private static OppoAnimationHelper makeLeftIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mLeftInList);
        if (helper != null) {
            return helper;
        }
        helper = createLeftIn(context, view, visibility, forceMenuBar, offset, tag);
        mLeftInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeLeftOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mLeftOutList);
        if (helper != null) {
            return helper;
        }
        helper = createLeftOut(context, view, visibility, forceMenuBar, offset, tag);
        mLeftOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeRightIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mRightInList);
        if (helper != null) {
            return helper;
        }
        helper = createRightIn(context, view, visibility, forceMenuBar, offset, tag);
        mRightInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeRightOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mRightOutList);
        if (helper != null) {
            return helper;
        }
        helper = createRightOut(context, view, visibility, forceMenuBar, offset, tag);
        mRightOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, boolean forceMenuBar, long offset) {
        return makeBottomIn(context, view, visibility, forceMenuBar, offset, makeTag(view, forceMenuBar, TAG_BOTTOM_IN));
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, boolean forceMenuBar, String tag) {
        return makeBottomIn(context, view, visibility, forceMenuBar, DEFAULT_DURATION_OFFSET, tag);
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, long offset, String tag) {
        return makeBottomIn(context, view, visibility, false, offset, tag);
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mBottomInList);
        if (helper != null) {
            return helper;
        }
        helper = createBottomIn(context, view, visibility, forceMenuBar, offset, tag);
        mBottomInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, boolean forceMenuBar, long offset) {
        return makeBottomOut(context, view, visibility, forceMenuBar, offset, makeTag(view, forceMenuBar, TAG_BOTTOM_OUT));
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, boolean forceMenuBar, String tag) {
        return makeBottomOut(context, view, visibility, forceMenuBar, DEFAULT_DURATION_OFFSET, tag);
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, long offset, String tag) {
        return makeBottomOut(context, view, visibility, false, offset, tag);
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mBottomOutList);
        if (helper != null) {
            return helper;
        }
        helper = createBottomOut(context, view, visibility, forceMenuBar, offset, tag);
        mBottomOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeFadeIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mFadeInList);
        if (helper != null) {
            return helper;
        }
        helper = createFadeIn(context, view, visibility, forceMenuBar, offset, tag);
        mFadeInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeFadeOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mFadeOutList);
        if (helper != null) {
            return helper;
        }
        helper = createFadeOut(context, view, visibility, forceMenuBar, offset, tag);
        mFadeOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper createAnimation(Context context, View view, int visibility, boolean forceMenuBar, boolean fillAfter, Animation animation, boolean in, long offset, String tag) {
        OppoAnimationHelper helper = new OppoAnimationHelper(animation, in, getBottomView(view, forceMenuBar), fillAfter, offset);
        helper.setFinalVisibility(visibility);
        helper.setTag(tag);
        return helper;
    }

    private static OppoAnimationHelper createAnimation(Context context, View view, int visibility, boolean forceMenuBar, boolean fillAfter, int res, boolean in, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, fillAfter, AnimationUtils.loadAnimation(context, res), in, offset, tag);
    }

    private static OppoAnimationHelper createBottomIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, true, 201982976, true, offset, tag);
    }

    private static OppoAnimationHelper createBottomOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982977, false, offset, tag);
    }

    private static OppoAnimationHelper createLeftIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982988, true, offset, tag);
    }

    private static OppoAnimationHelper createLeftOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982989, false, offset, tag);
    }

    private static OppoAnimationHelper createRightIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982978, true, offset, tag);
    }

    private static OppoAnimationHelper createRightOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982979, false, offset, tag);
    }

    private static OppoAnimationHelper createFadeIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982980, true, offset, tag);
    }

    private static OppoAnimationHelper createFadeOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982981, false, offset, tag);
    }

    private void updateMenuBar(View view, boolean refreshMore) {
    }

    private void showMajorGroup(View view, boolean show) {
    }

    private void showBottomExtra(View view, boolean show) {
        int i = 0;
        if (view != null) {
            ColorLog.e("log.key.multi_select.disp", this.mTagClass, new Object[]{"showBottomExtra : ", Boolean.valueOf(show)});
            if (!show) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    private View createFlowMenu(int layout) {
        return null;
    }

    private Activity initActivity(Activity activity) {
        if (activity == null) {
            throw new RuntimeException("Your activity is null");
        }
        activity.getApplication().registerActivityLifecycleCallbacks(this);
        return activity;
    }

    private View initBottomMenu(View view) {
        return view;
    }

    private void getBottomInfo() {
    }

    private ActionMode onStartActionMode() {
        if (this.mActionMode == null) {
            ColorLog.e("log.key.multi_select.disp", this.mTagClass, new Object[]{"onStartActionMode"});
            this.mStarting = true;
            this.mRunningList.clear();
            if (this.mBottomMenu != null) {
                startBottomAnimation(this.mFlowIn);
                if (needBottomAnimation()) {
                    startBottomAnimation(this.mBottomOut);
                    startBottomAnimation(this.mExtraOut);
                }
            }
            this.mActionMode = this.mActivity.startActionMode(this);
        }
        return this.mActionMode;
    }

    private void onFinishActionMode() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
    }

    private boolean isMoreGroupExpanded() {
        return false;
    }

    private boolean needBottomAnimation() {
        return isActionBarShow() ? isMoreGroupExpanded() ^ 1 : false;
    }

    private int getBottomInVisibility() {
        return 0;
    }

    private int getBottomOutVisibility() {
        return 8;
    }

    private int getFlowInVisibility() {
        return 0;
    }

    private int getFlowOutVisibility() {
        return 8;
    }

    private boolean getActionBarShow() {
        ActionBar actionBar = this.mActivity.getActionBar();
        if (actionBar != null) {
            return actionBar.isShowing();
        }
        return false;
    }

    private void initActionBar() {
        ActionBarCallback callback = (ActionBarCallback) this.mActivity.getActionBar();
        if (callback != null) {
            callback.addShowListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    OppoMultiSelectHelper.this.mActionBarAnimating = false;
                    if (OppoMultiSelectHelper.this.mStarting) {
                        OppoMultiSelectHelper.this.tryClearAnimations();
                    }
                }

                public void onAnimationStart(Animator animation) {
                    OppoMultiSelectHelper.this.mActionBarAnimating = true;
                }
            });
            callback.addHideListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    OppoMultiSelectHelper.this.mActionBarAnimating = false;
                    if (OppoMultiSelectHelper.this.mFinishing) {
                        OppoMultiSelectHelper.this.tryClearAnimations();
                    }
                }

                public void onAnimationStart(Animator animation) {
                    OppoMultiSelectHelper.this.mActionBarAnimating = true;
                }
            });
        }
    }

    private boolean isAnimationMatch(Animation animation) {
        for (OppoAnimationHelper helper : this.mRunningList) {
            if (helper.getAnimation() == animation) {
                return true;
            }
        }
        return false;
    }

    private boolean needCancelAnimation(OppoAnimationHelper helper) {
        String tag = helper.getTag();
        if (tag == null) {
            return false;
        }
        boolean z;
        if (tag.equals(makeTag(helper.getView(), false, TAG_RIGHT_IN))) {
            z = true;
        } else {
            z = tag.equals(makeTag(helper.getView(), false, TAG_RIGHT_OUT));
        }
        return z;
    }

    private boolean needClearAnimations() {
        if (this.mClearing) {
            return false;
        }
        boolean isRunning = this.mActionBarAnimating;
        for (OppoAnimationHelper helper : this.mRunningList) {
            if (needCancelAnimation(helper)) {
                helper.cancel();
            } else {
                isRunning |= helper.isRunning();
            }
        }
        return isRunning ^ 1;
    }

    private void tryClearAnimations() {
        if (needClearAnimations()) {
            doClearAnimations();
        }
    }

    private void clearAnimations() {
        while (this.mRunningList.size() > 0) {
            OppoAnimationHelper helper = (OppoAnimationHelper) this.mRunningList.get(0);
            helper.clear();
            ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"clearAnimation : ", helper.getTag(), " : ", helper.getView(), "=", Integer.valueOf(helper.getView().getVisibility())});
            this.mRunningList.remove(helper);
        }
    }

    private void startBottomAnimation(OppoAnimationHelper helper) {
        if (helper != null) {
            this.mRunningList.add(helper);
            ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"startBottomAnimation : ", helper.getTag()});
            View view = helper.getView();
            if (helper.getFillAfter()) {
                view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
                view.offsetTopAndBottom(view.getMeasuredHeight());
                ColorLog.e("log.key.multi_select.anim", this.mTagClass, new Object[]{"offsetTopAndBottom : ", Integer.valueOf(height)});
            }
            helper.start(false, this);
        }
    }
}

package com.color.actionbar.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import com.android.internal.R;
import com.color.animation.ColorAnimatorWrapper;
import com.color.widget.ColorActionModeCallback;
import com.color.widget.ColorBottomMenuCallback;
import com.color.widget.ColorPagerCallback;
import com.color.widget.ColorSpinnerCallback;
import com.color.widget.ColorSpinnerCallback.DropdownDismissCallback;
import java.util.List;

public class ColorActionBarUtil {
    private static final boolean DBG = false;
    private static final String TAG = "ColorActionBarUtil";
    static final String THEME_CUSTOM_ACTIONBAR = "oppo:Theme.OPPO.CustomActionBar";

    public interface ScrollTabCallback extends ColorPagerCallback {
        void updateTabScrollPosition(int i, float f, int i2);

        void updateTabScrollState(int i);
    }

    public interface ActionBarCallback extends ScrollTabCallback, ColorBottomMenuCallback, ColorSpinnerCallback {
        void addAfterAnimator(Animator animator);

        void addAfterAnimatorWrapper(ColorAnimatorWrapper colorAnimatorWrapper);

        void addAfterAnimatorWrappers(List<ColorAnimatorWrapper> list);

        void addAfterAnimators(List<Animator> list);

        void addBeforeAnimator(Animator animator);

        void addBeforeAnimatorWrapper(ColorAnimatorWrapper colorAnimatorWrapper);

        void addBeforeAnimatorWrappers(List<ColorAnimatorWrapper> list);

        void addBeforeAnimators(List<Animator> list);

        void addHideListener(AnimatorListener animatorListener);

        void addSearchViewHideListener(AnimatorListener animatorListener);

        void addSearchViewShowListener(AnimatorListener animatorListener);

        void addSearchViewWithAnimator(Animator animator);

        void addShowListener(AnimatorListener animatorListener);

        void addWithAnimator(Animator animator);

        void addWithAnimatorWrapper(ColorAnimatorWrapper colorAnimatorWrapper);

        void addWithAnimatorWrappers(List<ColorAnimatorWrapper> list);

        void addWithAnimators(List<Animator> list);

        void cancelShowHide();

        int getContentId();

        int getHomeId();

        boolean hasEmbeddedTabs();

        void setActionBarSubTitle(int i);

        void setActionBarSubTitle(CharSequence charSequence);

        void setActionMenuTextColor(ColorStateList colorStateList);

        void setActionModeAnim(boolean z);

        void setActionModeCallback(ColorActionModeCallback colorActionModeCallback);

        void setBackTitle(int i);

        void setBackTitle(CharSequence charSequence);

        void setBackTitleTextColor(ColorStateList colorStateList);

        void setColorBottomWindowContentOverlay(Drawable drawable);

        void setColorWindowContentOverlay(Drawable drawable);

        void setEmbeddedTabs(boolean z);

        void setHintText(int i);

        void setHintText(CharSequence charSequence);

        void setIgnoreColorBottomWindowContentOverlay(boolean z);

        void setIgnoreColorWindowContentOverlay(boolean z);

        void setMainActionBar(boolean z);

        void setSearchBarMode(boolean z);

        void setSplitActionBarBg(Drawable drawable);

        void setSplitActionBarOverlay(boolean z);

        void setSplitActionBarTextColor(ColorStateList colorStateList);

        void setSplitHideWithActionBar(boolean z);

        void setStatusBarActionBarBg(Drawable drawable);

        void setSubtitleTextColor(int i);

        void setTitleTextColor(int i);
    }

    public interface DelegateCallback {
        boolean hasWindowFeature(int i);

        void superSetContentView(int i);

        void superSetContentView(View view);

        boolean supportRequestWindowFeature(int i);
    }

    public interface ActivityCallback extends DelegateCallback {
        void setSupportProgress(int i);

        void setSupportProgressBarIndeterminate(boolean z);

        void setSupportProgressBarIndeterminateVisibility(boolean z);

        void setSupportProgressBarVisibility(boolean z);

        void setSupportSecondaryProgress(int i);
    }

    public interface DialogCallback extends DelegateCallback {
    }

    public static int getContentId(ActionBar actionBar) {
        if (actionBar instanceof ActionBarCallback) {
            return ((ActionBarCallback) actionBar).getContentId();
        }
        return 16908290;
    }

    public static int getHomeId(ActionBar actionBar) {
        if (actionBar instanceof ActionBarCallback) {
            return ((ActionBarCallback) actionBar).getHomeId();
        }
        return R.id.home;
    }

    public static int getBottomMenuViewId() {
        return 201458882;
    }

    public static void updateTabScrollPosition(ActionBar actionBar, int position, float positionOffset, int positionOffsetPixels) {
        if (actionBar instanceof ScrollTabCallback) {
            ((ScrollTabCallback) actionBar).updateTabScrollPosition(position, positionOffset, positionOffsetPixels);
        }
    }

    public static void updateTabScrollState(ActionBar actionBar, int state) {
        if (actionBar instanceof ScrollTabCallback) {
            ((ScrollTabCallback) actionBar).updateTabScrollState(state);
        }
    }

    public static void updateMenuScrollPosition(ActionBar actionBar, int index, float offset) {
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).updateMenuScrollPosition(index, offset);
        }
    }

    public static void updateMenuScrollState(ActionBar actionBar, int state) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).updateMenuScrollState(state);
        }
    }

    public static void updateMenuScrollData(ActionBar actionBar) {
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).updateMenuScrollData();
        }
    }

    public static void setMenuUpdateMode(ActionBar actionBar, int mode) {
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).setMenuUpdateMode(mode);
        }
    }

    public static void lockMenuUpdate(ActionBar actionBar) {
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).lockMenuUpdate();
        }
    }

    public static void unlockMenuUpdate(ActionBar actionBar) {
        if (actionBar instanceof ColorBottomMenuCallback) {
            ((ColorBottomMenuCallback) actionBar).unlockMenuUpdate();
        }
    }

    public static void setDropdownDismissCallback(ActionBar actionBar, DropdownDismissCallback callback) {
        if (actionBar instanceof ColorSpinnerCallback) {
            ((ColorSpinnerCallback) actionBar).setDropdownDismissCallback(callback);
        }
    }

    public static void setDropdownItemClickListener(ActionBar actionBar, OnItemClickListener listener) {
        if (actionBar instanceof ColorSpinnerCallback) {
            ((ColorSpinnerCallback) actionBar).setDropdownItemClickListener(listener);
        }
    }

    public static void setDropdownUpdateAfterAnim(ActionBar actionBar, boolean update) {
        if (actionBar instanceof ColorSpinnerCallback) {
            ((ColorSpinnerCallback) actionBar).setDropdownUpdateAfterAnim(update);
        }
    }

    public static void setHasEmbeddedTabs(ActionBar actionBar, boolean hasEmbeddedTabs) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setEmbeddedTabs(hasEmbeddedTabs);
        }
    }

    public static void setSplitActionBarOverlay(ActionBar actionBar, boolean overlay) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setSplitActionBarOverlay(overlay);
        }
    }

    public static void setSplitHideWithActionBar(ActionBar actionBar, boolean hideWith) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setSplitHideWithActionBar(hideWith);
        }
    }

    public static boolean hasEmbeddedTabs(ActionBar actionBar) {
        if (actionBar instanceof ActionBarCallback) {
            return ((ActionBarCallback) actionBar).hasEmbeddedTabs();
        }
        return false;
    }

    public static void setColorWindowContentOverlay(ActionBar actionBar, Drawable overlay) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setColorWindowContentOverlay(overlay);
        }
    }

    public static void setColorBottomWindowContentOverlay(ActionBar actionBar, Drawable bottomOverlay) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setColorBottomWindowContentOverlay(bottomOverlay);
        }
    }

    public static void setIgnoreColorWindowContentOverlay(ActionBar actionBar, boolean isIgnore) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setIgnoreColorWindowContentOverlay(isIgnore);
        }
    }

    public static void setIgnoreColorBottomWindowContentOverlay(ActionBar actionBar, boolean isIgnore) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setIgnoreColorBottomWindowContentOverlay(isIgnore);
        }
    }

    public static void setStatusBarActionBarBg(ActionBar actionBar, Drawable bg) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setStatusBarActionBarBg(bg);
        }
    }

    public static void setSplitActionBarBg(ActionBar actionBar, Drawable bg) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setSplitActionBarBg(bg);
        }
    }

    public static void setSplitActionBarTextColor(ActionBar actionBar, ColorStateList textColor) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setSplitActionBarTextColor(textColor);
        }
    }

    public static void setBackTitleTextColor(ActionBar actionBar, ColorStateList textColor) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setBackTitleTextColor(textColor);
        }
    }

    public static void setTitleTextColor(ActionBar actionBar, int textColor) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setTitleTextColor(textColor);
        }
    }

    public static void setSubtitleTextColor(ActionBar actionBar, int textColor) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setSubtitleTextColor(textColor);
        }
    }

    public static void setActionMenuTextColor(ActionBar actionBar, ColorStateList textColor) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setActionMenuTextColor(textColor);
        }
    }

    public static boolean requestWindowFeature(Activity activity, int featureId) {
        if (activity instanceof ActivityCallback) {
            return ((ActivityCallback) activity).supportRequestWindowFeature(featureId);
        }
        return activity.requestWindowFeature(featureId);
    }

    public static void setProgressBarVisibility(Activity activity, boolean visible) {
        if (activity instanceof ActivityCallback) {
            ((ActivityCallback) activity).setSupportProgressBarVisibility(visible);
        } else {
            activity.setProgressBarVisibility(visible);
        }
    }

    public static void setProgressBarIndeterminateVisibility(Activity activity, boolean visible) {
        if (activity instanceof ActivityCallback) {
            ((ActivityCallback) activity).setSupportProgressBarIndeterminateVisibility(visible);
        } else {
            activity.setProgressBarIndeterminateVisibility(visible);
        }
    }

    public static void setProgressBarIndeterminate(Activity activity, boolean indeterminate) {
        if (activity instanceof ActivityCallback) {
            ((ActivityCallback) activity).setSupportProgressBarIndeterminate(indeterminate);
        } else {
            activity.setProgressBarIndeterminate(indeterminate);
        }
    }

    public static void setProgress(Activity activity, int progress) {
        if (activity instanceof ActivityCallback) {
            ((ActivityCallback) activity).setSupportProgress(progress);
        } else {
            activity.setProgress(progress);
        }
    }

    public static void setSecondaryProgress(Activity activity, int progress) {
        if (activity instanceof ActivityCallback) {
            ((ActivityCallback) activity).setSupportSecondaryProgress(progress);
        } else {
            activity.setSecondaryProgress(progress);
        }
    }

    public static void setBackTitle(ActionBar actionBar, CharSequence title) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setBackTitle(title);
        }
    }

    public static void setBackTitle(ActionBar actionBar, int resId) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setBackTitle(resId);
        }
    }

    public static void setHintText(ActionBar actionBar, CharSequence hintText) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setHintText(hintText);
        }
    }

    public static void setHintText(ActionBar actionBar, int resId) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setHintText(resId);
        }
    }

    public static void setActionBarSubTitle(ActionBar actionBar, CharSequence subtitle) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setActionBarSubTitle(subtitle);
        }
    }

    public static void setActionBarSubTitle(ActionBar actionBar, int resId) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setActionBarSubTitle(resId);
        }
    }

    public static void setMainActionBar(ActionBar actionBar, boolean isMain) {
        if (actionBar instanceof ActionBarCallback) {
            ((ActionBarCallback) actionBar).setMainActionBar(isMain);
        }
    }
}

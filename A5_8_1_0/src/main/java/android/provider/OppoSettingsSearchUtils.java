package android.provider;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class OppoSettingsSearchUtils {
    public static final String ARGS_COLOR_CATEGORY = ":settings:fragment_args_color_category";
    public static final String ARGS_COLOR_PREFERENCE = ":settings:fragment_args_color_preferece";
    public static final String ARGS_HIGHT_LIGHT_TIME = ":settings:fragment_args_light_time";
    public static final String ARGS_KEY = ":settings:fragment_args_key";
    public static final String ARGS_WAIT_TIME = ":settings:fragment_args_wait_time";
    private static final int DELAY_TIME = 150;
    public static final int HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT = -1776412;
    public static final int HIGH_LIGHT_TIME_DEFAULT = 1000;
    private static final int LAST_TIME = 500;
    public static final String RAW_RENAME_EXTRA_KEY = "_settings_extra_key";
    private static final int START_TIME = 100;
    private static final int STOP_TIME = 250;
    public static final int WAIT_TIME_DEFAULT = 300;

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void highlightListView(final ListView listView, final int position, final boolean isCategory, Intent intent) {
        if (!(listView == null || intent == null || TextUtils.isEmpty(intent.getStringExtra(ARGS_KEY)) || listView == null)) {
            listView.post(new Runnable() {
                public void run() {
                    if (position >= 0) {
                        listView.setSelection(position);
                        OppoSettingsSearchUtils.showHightlight(listView, position, OppoSettingsSearchUtils.HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT, isCategory);
                    }
                }
            });
        }
    }

    public static void highlightPreference(PreferenceScreen preferenceScreen, ListView listView, Bundle bundle) {
        if (preferenceScreen != null && listView != null && bundle != null) {
            String argsKey = bundle.getString(ARGS_KEY);
            if (!TextUtils.isEmpty(argsKey)) {
                calculateHightlight(preferenceScreen, listView, argsKey, (int) HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT);
            }
        }
    }

    public static void highlightPreference(ListView listView, Bundle bundle) {
        if (listView != null && bundle != null) {
            String argsKey = bundle.getString(ARGS_KEY);
            if (!TextUtils.isEmpty(argsKey)) {
                calculateHightlight(listView, argsKey, (int) HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT, false);
            }
        }
    }

    private static void calculateHightlight(final ListView listView, final String argsKey, final int argsColorPreference, final boolean isCategory) {
        if (listView != null) {
            listView.post(new Runnable() {
                public void run() {
                    int position = OppoSettingsSearchUtils.canUseListViewForHighLighting(listView, argsKey);
                    if (position > 1) {
                        listView.setSelection(position);
                    }
                    if (position >= 0) {
                        OppoSettingsSearchUtils.showHightlight(listView, position, argsColorPreference, isCategory);
                    }
                }
            });
        }
    }

    private static void calculateHightlight(PreferenceScreen preferenceScreen, ListView listView, String argsKey, int argsColorPreference) {
        Preference pre = preferenceScreen.findPreference(argsKey);
        if (pre != null) {
            boolean isCategory;
            if (pre instanceof PreferenceCategory) {
                isCategory = true;
            } else {
                isCategory = false;
            }
            int backgroundColor = argsColorPreference;
            calculateHightlight(listView, argsKey, argsColorPreference, isCategory);
        }
    }

    private static void showHightlight(final ListView listView, final int position, final int backgroundColor, boolean isCategory) {
        if (!isCategory) {
            listView.postDelayed(new Runnable() {
                public void run() {
                    int index = position - listView.getFirstVisiblePosition();
                    if (index >= 0 && index < listView.getChildCount()) {
                        final View view = listView.getChildAt(index);
                        if (view != null) {
                            final Drawable drawable = view.getBackground();
                            AnimationDrawable animationDrawable = OppoSettingsSearchUtils.getAnimationDrawable(backgroundColor, drawable);
                            view.setBackgroundDrawable(animationDrawable);
                            animationDrawable.start();
                            view.postDelayed(new Runnable() {
                                public void run() {
                                    view.setBackground(drawable);
                                }
                            }, 1000);
                        }
                    }
                }
            }, 300);
        }
    }

    private static AnimationDrawable getAnimationDrawable(int backgroundColor, Drawable sourceDrable) {
        int i;
        double alpha;
        ColorDrawable drawable;
        AnimationDrawable animationDrawable = new AnimationDrawable();
        for (i = 0; i < 6; i++) {
            alpha = ((((double) i) + 0.0d) * 255.0d) / 6.0d;
            drawable = new ColorDrawable(backgroundColor);
            drawable.setAlpha((int) alpha);
            if (sourceDrable != null) {
                animationDrawable.addFrame(new LayerDrawable(new Drawable[]{sourceDrable, drawable}), 16);
            } else {
                animationDrawable.addFrame(drawable, 16);
            }
        }
        animationDrawable.addFrame(new ColorDrawable(backgroundColor), 250);
        for (i = 0; i < 31; i++) {
            alpha = ((((double) (31 - i)) - 0.0d) * 255.0d) / 31.0d;
            drawable = new ColorDrawable(backgroundColor);
            drawable.setAlpha((int) alpha);
            if (sourceDrable != null) {
                animationDrawable.addFrame(new LayerDrawable(new Drawable[]{sourceDrable, drawable}), 16);
            } else {
                animationDrawable.addFrame(drawable, 16);
            }
        }
        if (sourceDrable != null) {
            animationDrawable.addFrame(sourceDrable, 150);
        }
        return animationDrawable;
    }

    private static int canUseListViewForHighLighting(ListView listView, String key) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return -1;
        }
        int count = adapter.getCount();
        for (int n = 0; n < count; n++) {
            Preference item = adapter.getItem(n);
            if (item instanceof Preference) {
                String preferenceKey = item.getKey();
                if (preferenceKey != null && preferenceKey.equals(key)) {
                    return n;
                }
            }
        }
        return -1;
    }
}

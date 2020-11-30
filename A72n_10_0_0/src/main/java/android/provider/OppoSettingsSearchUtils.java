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

    public static void highlightListView(ListView listView, int position, boolean isCategory, Intent intent) {
        highlightListView(listView, position, isCategory, intent, 0);
    }

    public static void highlightListView(final ListView listView, final int position, final boolean isCategory, Intent intent, final int y) {
        if (listView != null && intent != null && !TextUtils.isEmpty(intent.getStringExtra(ARGS_KEY))) {
            listView.post(new Runnable() {
                /* class android.provider.OppoSettingsSearchUtils.AnonymousClass1 */

                public void run() {
                    int i = position;
                    if (i >= 0) {
                        int i2 = y;
                        if (i2 == 0) {
                            listView.setSelection(i);
                        } else {
                            listView.setSelectionFromTop(i, i2);
                        }
                        OppoSettingsSearchUtils.showHightlight(listView, position, OppoSettingsSearchUtils.HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT, isCategory);
                    }
                }
            });
        }
    }

    public static void highlightPreference(PreferenceScreen preferenceScreen, ListView listView, Bundle bundle) {
        highlightPreference(preferenceScreen, listView, bundle, 0);
    }

    public static void highlightPreference(PreferenceScreen preferenceScreen, ListView listView, Bundle bundle, int y) {
        if (preferenceScreen != null && listView != null && bundle != null) {
            String argsKey = bundle.getString(ARGS_KEY);
            if (!TextUtils.isEmpty(argsKey)) {
                calculateHightlight(preferenceScreen, listView, argsKey, (int) HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT, y);
            }
        }
    }

    public static void highlightPreference(ListView listView, Bundle bundle) {
        highlightPreference(listView, bundle, 0);
    }

    public static void highlightPreference(ListView listView, Bundle bundle, int y) {
        if (listView != null && bundle != null) {
            String argsKey = bundle.getString(ARGS_KEY);
            if (!TextUtils.isEmpty(argsKey)) {
                calculateHightlight(listView, argsKey, (int) HIGHT_LIGHT_COLOR_PREFERENCE_DEFAULT, false, y);
            }
        }
    }

    private static void calculateHightlight(final ListView listView, final String argsKey, final int argsColorPreference, final boolean isCategory, final int y) {
        if (listView != null) {
            listView.post(new Runnable() {
                /* class android.provider.OppoSettingsSearchUtils.AnonymousClass2 */

                public void run() {
                    int position = OppoSettingsSearchUtils.canUseListViewForHighLighting(ListView.this, argsKey);
                    if (position > 1) {
                        int i = y;
                        if (i == 0) {
                            ListView.this.setSelection(position);
                        } else {
                            ListView.this.setSelectionFromTop(position, i);
                        }
                    }
                    if (position >= 0) {
                        OppoSettingsSearchUtils.showHightlight(ListView.this, position, argsColorPreference, isCategory);
                    }
                }
            });
        }
    }

    private static void calculateHightlight(PreferenceScreen preferenceScreen, ListView listView, String argsKey, int argsColorPreference, int y) {
        boolean isCategory;
        Preference pre = preferenceScreen.findPreference(argsKey);
        if (pre != null) {
            if (pre instanceof PreferenceCategory) {
                isCategory = true;
            } else {
                isCategory = false;
            }
            calculateHightlight(listView, argsKey, argsColorPreference, isCategory, y);
        }
    }

    /* access modifiers changed from: private */
    public static void showHightlight(final ListView listView, final int position, final int backgroundColor, boolean isCategory) {
        if (!isCategory) {
            listView.postDelayed(new Runnable() {
                /* class android.provider.OppoSettingsSearchUtils.AnonymousClass3 */

                /* JADX WARNING: Code restructure failed: missing block: B:4:0x0013, code lost:
                    r1 = r3.getChildAt(r0);
                 */
                public void run() {
                    final View view;
                    int index = position - listView.getFirstVisiblePosition();
                    if (index >= 0 && index < listView.getChildCount() && view != null) {
                        final Drawable drawable = view.getBackground();
                        AnimationDrawable animationDrawable = OppoSettingsSearchUtils.getAnimationDrawable(backgroundColor, drawable);
                        view.setBackgroundDrawable(animationDrawable);
                        animationDrawable.start();
                        view.postDelayed(new Runnable() {
                            /* class android.provider.OppoSettingsSearchUtils.AnonymousClass3.AnonymousClass1 */

                            public void run() {
                                view.setBackground(drawable);
                            }
                        }, 1000);
                    }
                }
            }, 300);
        }
    }

    /* access modifiers changed from: private */
    public static AnimationDrawable getAnimationDrawable(int backgroundColor, Drawable sourceDrable) {
        int i;
        double d;
        boolean z;
        char c;
        AnimationDrawable animationDrawable = new AnimationDrawable();
        int i2 = 0;
        while (true) {
            i = 2;
            d = 0.0d;
            if (i2 >= 6) {
                break;
            }
            ColorDrawable drawable = new ColorDrawable(backgroundColor);
            drawable.setAlpha((int) (((((double) i2) + 0.0d) * 255.0d) / ((double) 6)));
            if (sourceDrable != null) {
                animationDrawable.addFrame(new LayerDrawable(new Drawable[]{sourceDrable, drawable}), 16);
            } else {
                animationDrawable.addFrame(drawable, 16);
            }
            i2++;
        }
        animationDrawable.addFrame(new ColorDrawable(backgroundColor), 250);
        int i3 = 0;
        while (i3 < 31) {
            double alpha = ((((double) (31 - i3)) - d) * 255.0d) / ((double) 31);
            ColorDrawable drawable2 = new ColorDrawable(backgroundColor);
            drawable2.setAlpha((int) alpha);
            if (sourceDrable != null) {
                Drawable[] drawableArr = new Drawable[i];
                drawableArr[0] = sourceDrable;
                z = true;
                drawableArr[1] = drawable2;
                LayerDrawable ld = new LayerDrawable(drawableArr);
                c = 16;
                animationDrawable.addFrame(ld, 16);
            } else {
                c = 16;
                z = true;
                animationDrawable.addFrame(drawable2, 16);
                if (i3 == 31 - 1) {
                    animationDrawable.addFrame(new ColorDrawable(0), 300);
                }
            }
            i3++;
            i = 2;
            d = 0.0d;
        }
        if (sourceDrable != null) {
            animationDrawable.addFrame(sourceDrable, 150);
        }
        return animationDrawable;
    }

    /* access modifiers changed from: private */
    public static int canUseListViewForHighLighting(ListView listView, String key) {
        String preferenceKey;
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return -1;
        }
        int count = adapter.getCount();
        for (int n = 0; n < count; n++) {
            Object item = adapter.getItem(n);
            if ((item instanceof Preference) && (preferenceKey = ((Preference) item).getKey()) != null && preferenceKey.equals(key)) {
                return n;
            }
        }
        return -1;
    }
}

package com.color.inner.preference;

import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PreferenceWrapper {
    private static final String TAG = "PreferenceWrapper";

    private PreferenceWrapper() {
    }

    public static void performClick(Preference preference, PreferenceScreen preferenceScreen) {
        try {
            preference.performClick(preferenceScreen);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}

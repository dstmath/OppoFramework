package com.color.inner.internal.policy;

import android.content.Context;
import android.view.Window;
import com.android.internal.policy.PhoneWindow;

public class PhoneWindowWrapper {
    private static final String TAG = "PhoneWindowWrapper";
    private Window mWindow;

    public PhoneWindowWrapper(Context context) {
        this.mWindow = new PhoneWindow(context);
    }

    public Window getWindow() {
        return this.mWindow;
    }
}

package com.android.server.policy;

import android.os.Debug;
import android.os.IBinder;
import android.util.Slog;
import android.view.View;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;

class SplashScreenSurface implements WindowManagerPolicy.StartingSurface {
    private static final String TAG = "WindowManager";
    private final IBinder mAppToken;
    private final View mView;

    SplashScreenSurface(View view, IBinder appToken) {
        this.mView = view;
        this.mAppToken = appToken;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.StartingSurface
    public void remove() {
        if (PhoneWindowManager.DEBUG_SPLASH_SCREEN) {
            Slog.v("WindowManager", "Removing splash screen window for " + this.mAppToken + ": " + this + " Callers=" + Debug.getCallers(4));
        }
        ((WindowManager) this.mView.getContext().getSystemService(WindowManager.class)).removeView(this.mView);
    }
}

package com.android.server.policy;

import android.app.IColorKeyguardSessionCallback;
import android.os.IBinder;

public interface OppoWindowManagerPolicy {
    boolean doesNeedWaitingKeyguard();

    boolean isStatusBarVisible();

    boolean openKeyguardSession(IColorKeyguardSessionCallback iColorKeyguardSessionCallback, IBinder iBinder, String str);

    void requestDismissKeyguard();

    void requestKeyguard(String str);
}

package android.view;

import android.app.IColorKeyguardSessionCallback;
import android.os.IBinder;

public interface OppoWindowManagerPolicy extends WindowManagerPolicy {
    boolean doesNeedWaitingKeyguard();

    boolean isKeyguardShowingAndNotOccludedComp();

    boolean isNavigationBarVisible();

    boolean isShortcutsPanelShow();

    boolean isStatusBarVisible();

    boolean openKeyguardSession(IColorKeyguardSessionCallback iColorKeyguardSessionCallback, IBinder iBinder, String str);

    void requestDismissKeyguard();

    void requestKeyguard(String str);
}

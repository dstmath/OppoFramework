package com.android.server.wm;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.theia.NoFocusWindow;
import com.oppo.uifirst.UIFirstUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public class OppoScreenFrozenBooster {
    private static final boolean DEBUG = (!SystemProperties.getBoolean("ro.build.release_type", false));
    private static final String TAG = "OppoScreenFrozenBooster";
    private static OppoScreenFrozenBooster sInstance;
    private ArrayList<WindowProcessController> mUxBoostList = new ArrayList<>();
    private WindowManagerService mWms = WindowManagerService.getInstance();

    public static OppoScreenFrozenBooster getInstance() {
        if (sInstance == null) {
            sInstance = new OppoScreenFrozenBooster();
        }
        return sInstance;
    }

    private OppoScreenFrozenBooster() {
    }

    public WindowProcessController getWindowProcessController(WindowState w) {
        WindowState parentWindow = w.getParentWindow();
        Session session = parentWindow != null ? parentWindow.mSession : w.mSession;
        if (session.mPid == ActivityManagerService.MY_PID || session.mPid < 0) {
            return null;
        }
        return this.mWms.mAtmService.getProcessController(session.mPid, session.mUid);
    }

    public void boost(boolean enable) {
        synchronized (this.mWms.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (enable) {
                    this.mWms.mRoot.forAllWindows((Consumer<WindowState>) new Consumer() {
                        /* class com.android.server.wm.$$Lambda$OppoScreenFrozenBooster$31kGNayNrnL37Fw9HPk1pm8PUOs */

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            OppoScreenFrozenBooster.this.lambda$boost$0$OppoScreenFrozenBooster((WindowState) obj);
                        }
                    }, true);
                } else {
                    Iterator<WindowProcessController> it = this.mUxBoostList.iterator();
                    while (it.hasNext()) {
                        WindowProcessController app = it.next();
                        if (app.getCurrentProcState() != 2) {
                            UIFirstUtils.setUxThread(app.getPid(), app.getPid(), "0");
                            if (DEBUG) {
                                Slog.d(TAG, "deboost ->" + app.getPid());
                            }
                        }
                    }
                    this.mUxBoostList.clear();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public /* synthetic */ void lambda$boost$0$OppoScreenFrozenBooster(WindowState w) {
        WindowProcessController app = getWindowProcessController(w);
        if (app != null) {
            if (DEBUG) {
                Slog.d(TAG, "add to boost list:" + w + " in pid " + app);
            }
            this.mUxBoostList.add(app);
            UIFirstUtils.setUxThread(app.getPid(), app.getPid(), NoFocusWindow.HUNG_CONFIG_ENABLE);
        }
    }
}

package com.oppo.antiburn;

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewRootImpl;

public class OppoAntiBurnInjector {
    private static final String TAG = "OppoAntiBurnInjector";
    private static IOppoAntiBurnManager mOppoAntiBurnManager = null;
    private static boolean sFeatureEnable = true;

    private static boolean ensureValid() {
        if (!sFeatureEnable) {
            return false;
        }
        if (mOppoAntiBurnManager == null) {
            mOppoAntiBurnManager = new OppoAntiBurnManagerImpl();
        }
        if (mOppoAntiBurnManager != null) {
            return true;
        }
        return false;
    }

    public static void init(Application application) {
        if (ensureValid()) {
            mOppoAntiBurnManager.init(application);
        }
    }

    public static void handleOnlineConfig(String jsonStr) {
        if (ensureValid()) {
            mOppoAntiBurnManager.scheduleUpdateForceDarkConfig(jsonStr);
        }
    }

    public static void onActivityResume(Activity activity) {
        if (ensureValid()) {
            mOppoAntiBurnManager.onActivityResume(activity);
        }
    }

    public static void executeOPFDSpecialConfigAction(View v, Canvas canvas) {
        if (ensureValid()) {
            mOppoAntiBurnManager.executeOPFDSpecialConfigAction(v, canvas);
        }
    }

    public static void initViewTreeFlag(ViewRootImpl viewRoot, View decor) {
        if (ensureValid()) {
            mOppoAntiBurnManager.initViewTreeFlag(viewRoot, decor);
        }
    }
}

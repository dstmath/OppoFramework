package com.oppo.antiburn;

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewRootImpl;

public interface IOppoAntiBurnManager {
    void executeOPFDSpecialConfigAction(View view, Canvas canvas);

    void init(Application application);

    void initViewTreeFlag(ViewRootImpl viewRootImpl, View view);

    void onActivityResume(Activity activity);

    void scheduleUpdateForceDarkConfig(String str);
}

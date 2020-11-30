package com.oppo.app;

import android.content.Intent;
import android.os.Bundle;
import com.oppo.app.IOppoGameSpaceController;

public class OppoGameSpaceController extends IOppoGameSpaceController.Stub {
    @Override // com.oppo.app.IOppoGameSpaceController
    public void gameStarting(Intent intent, String pkg, boolean isResume) {
    }

    @Override // com.oppo.app.IOppoGameSpaceController
    public void gameExiting(String pkg) {
    }

    @Override // com.oppo.app.IOppoGameSpaceController
    public void videoStarting(Intent intent, String pkg) {
    }

    @Override // com.oppo.app.IOppoGameSpaceController
    public void dispatchGameDock(Bundle bundle) {
    }

    @Override // com.oppo.app.IOppoGameSpaceController
    public boolean isGameDockAllowed() {
        return true;
    }
}

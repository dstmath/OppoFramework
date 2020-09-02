package com.oppo.app;

import com.oppo.app.IOppoAppStartController;

public class OppoAppStartController extends IOppoAppStartController.Stub {
    @Override // com.oppo.app.IOppoAppStartController
    public void appStartMonitor(String pkgName, String exceptionClass, String exceptionMsg, String exceptionTrace, String monitorType) {
    }

    @Override // com.oppo.app.IOppoAppStartController
    public void preventStartMonitor(String callerPkg, String calledPkg, String startMode, String preventMode, String reason) {
    }

    @Override // com.oppo.app.IOppoAppStartController
    public void notifyPreventIndulge(String pkgName) {
    }
}

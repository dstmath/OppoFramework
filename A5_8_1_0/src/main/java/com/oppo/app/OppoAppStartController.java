package com.oppo.app;

import com.oppo.app.IOppoAppStartController.Stub;

public class OppoAppStartController extends Stub {
    public void appStartMonitor(String pkgName, String exceptionClass, String exceptionMsg, String exceptionTrace, String monitorType) {
    }

    public void preventStartMonitor(String callerPkg, String calledPkg, String startMode, String preventMode, String reason) {
    }
}

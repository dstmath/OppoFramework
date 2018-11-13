package com.qti.location.sdk;

import android.os.Bundle;

public interface IZatDebugReportingService {

    public interface IZatDebugReportCallback {
        void onDebugReportAvailable(Bundle bundle);
    }

    void deregisterForDebugReports(IZatDebugReportCallback iZatDebugReportCallback) throws IZatIllegalArgumentException;

    Bundle getDebugReport();

    void registerForDebugReports(IZatDebugReportCallback iZatDebugReportCallback) throws IZatIllegalArgumentException;
}

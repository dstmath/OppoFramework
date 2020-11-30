package com.android.server.am;

import android.app.IApplicationThread;

public interface OppoAppThreadExtendCallback {
    void dispatchOnlineConfig(IApplicationThread iApplicationThread, String str);
}

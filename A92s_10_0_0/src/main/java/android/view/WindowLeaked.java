package android.view;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidRuntimeException;

/* compiled from: WindowManagerGlobal */
final class WindowLeaked extends AndroidRuntimeException {
    @UnsupportedAppUsage
    public WindowLeaked(String msg) {
        super(msg);
    }
}

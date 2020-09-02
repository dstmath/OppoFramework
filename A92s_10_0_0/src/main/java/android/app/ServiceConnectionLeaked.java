package android.app;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidRuntimeException;

/* compiled from: LoadedApk */
final class ServiceConnectionLeaked extends AndroidRuntimeException {
    @UnsupportedAppUsage
    public ServiceConnectionLeaked(String msg) {
        super(msg);
    }
}

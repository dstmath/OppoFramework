package android.app;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidRuntimeException;

/* compiled from: LoadedApk */
final class IntentReceiverLeaked extends AndroidRuntimeException {
    @UnsupportedAppUsage
    public IntentReceiverLeaked(String msg) {
        super(msg);
    }
}

package androidx.fragment.app;

import android.util.AndroidRuntimeException;

/* access modifiers changed from: package-private */
public final class SuperNotCalledException extends AndroidRuntimeException {
    public SuperNotCalledException(String msg) {
        super(msg);
    }
}

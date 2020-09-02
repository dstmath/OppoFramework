package android.os;

import android.annotation.UnsupportedAppUsage;

/* compiled from: ZygoteProcess */
class ZygoteStartFailedEx extends Exception {
    @UnsupportedAppUsage
    ZygoteStartFailedEx(String s) {
        super(s);
    }

    @UnsupportedAppUsage
    ZygoteStartFailedEx(Throwable cause) {
        super(cause);
    }

    ZygoteStartFailedEx(String s, Throwable cause) {
        super(s, cause);
    }
}

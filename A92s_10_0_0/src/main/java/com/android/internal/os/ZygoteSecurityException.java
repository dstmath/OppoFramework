package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;

class ZygoteSecurityException extends RuntimeException {
    @UnsupportedAppUsage
    ZygoteSecurityException(String message) {
        super(message);
    }
}

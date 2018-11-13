package com.qti.location.sdk;

public class IZatFeatureNotSupportedException extends RuntimeException {
    public IZatFeatureNotSupportedException(String error) {
        super(error);
    }

    public IZatFeatureNotSupportedException(String error, Throwable cause) {
        super(error, cause);
    }
}

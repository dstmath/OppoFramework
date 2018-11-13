package com.qti.location.sdk;

public class IZatIllegalArgumentException extends RuntimeException {
    public IZatIllegalArgumentException(String error) {
        super(error);
    }

    public IZatIllegalArgumentException(String error, Throwable cause) {
        super(error, cause);
    }
}

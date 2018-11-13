package com.qti.location.sdk;

public class IZatStaleDataException extends RuntimeException {
    public IZatStaleDataException(String error) {
        super(error);
    }

    public IZatStaleDataException(String error, Throwable cause) {
        super(error, cause);
    }
}

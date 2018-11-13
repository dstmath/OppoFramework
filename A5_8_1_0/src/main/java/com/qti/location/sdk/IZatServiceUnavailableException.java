package com.qti.location.sdk;

public class IZatServiceUnavailableException extends RuntimeException {
    public IZatServiceUnavailableException(String error) {
        super(error);
    }

    public IZatServiceUnavailableException(String error, Throwable cause) {
        super(error, cause);
    }
}
